import Foundation
import Shared
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import UIKit
import AuthenticationServices
import CryptoKit

/// Kotlin `IosSocialAuthDelegate` ni Firebase + GoogleSignIn + Apple orqali amalga oshiradi.
/// `iOSApp.init()` da `IosSocialAuthBridge.shared.delegate = SocialAuthBridge()` deb ulanadi.
final class SocialAuthBridge: NSObject, IosSocialAuthDelegate {

    private var phoneVerificationId: String?

    // Apple oqimi uchun holat
    private var appleCompletion: ((IosAuthUser?, String?) -> Void)?
    private var currentNonce: String?

    // Telegram web-auth sessiyasiga kuchli havola
    private var webAuthSession: ASWebAuthenticationSession?

    // MARK: - Google

    func signInWithGoogle(onResult: @escaping (IosAuthUser?, String?) -> Void) {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            onResult(nil, "Firebase clientID topilmadi")
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)

        guard let rootVC = Self.rootViewController() else {
            onResult(nil, "Root view controller topilmadi")
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { signInResult, error in
            if let error = error {
                // Foydalanuvchi bekor qilsa ham error keladi -> user=nil, error=nil (Cancelled)
                let nsError = error as NSError
                if nsError.code == GIDSignInError.canceled.rawValue {
                    onResult(nil, nil)
                } else {
                    onResult(nil, error.localizedDescription)
                }
                return
            }
            guard
                let user = signInResult?.user,
                let idToken = user.idToken?.tokenString
            else {
                onResult(nil, "Google token olinmadi")
                return
            }
            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: user.accessToken.tokenString
            )
            Auth.auth().signIn(with: credential) { authResult, err in
                if let err = err {
                    onResult(nil, err.localizedDescription)
                    return
                }
                onResult(Self.map(authResult?.user, provider: "google"), nil)
            }
        }
    }

    // MARK: - Apple

    func signInWithApple(onResult: @escaping (IosAuthUser?, String?) -> Void) {
        let nonce = Self.randomNonceString()
        currentNonce = nonce
        appleCompletion = onResult

        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = Self.sha256(nonce)

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    // MARK: - Telegram

    func signInWithTelegram(url: String, callbackScheme: String, onResult: @escaping (IosAuthUser?, String?) -> Void) {
        guard let authURL = URL(string: url) else {
            onResult(nil, "Telegram URL noto‘g‘ri")
            return
        }
        let session = ASWebAuthenticationSession(url: authURL, callbackURLScheme: callbackScheme) { callbackURL, error in
            if let error = error {
                if (error as NSError).code == ASWebAuthenticationSessionError.canceledLogin.rawValue {
                    onResult(nil, nil) // Cancelled
                } else {
                    onResult(nil, error.localizedDescription)
                }
                return
            }
            guard
                let callbackURL = callbackURL,
                let token = URLComponents(url: callbackURL, resolvingAgainstBaseURL: false)?
                    .queryItems?.first(where: { $0.name == "token" })?.value
            else {
                onResult(nil, "Telegram token olinmadi")
                return
            }
            Auth.auth().signIn(withCustomToken: token) { authResult, err in
                if let err = err {
                    onResult(nil, err.localizedDescription)
                    return
                }
                onResult(Self.map(authResult?.user, provider: "telegram"), nil)
            }
        }
        session.presentationContextProvider = self
        session.prefersEphemeralWebBrowserSession = false
        webAuthSession = session
        session.start()
    }

    // MARK: - Phone (OTP)

    func sendOtp(phoneNumber: String, onResult: @escaping (KotlinBoolean, IosAuthUser?, String?) -> Void) {
        PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { [weak self] verificationID, error in
            if let error = error {
                onResult(false, nil, error.localizedDescription)
                return
            }
            self?.phoneVerificationId = verificationID
            onResult(true, nil, nil) // SMS yuborildi
        }
    }

    func confirmOtp(code: String, onResult: @escaping (IosAuthUser?, String?) -> Void) {
        guard let verificationID = phoneVerificationId else {
            onResult(nil, "Avval kod yuboring")
            return
        }
        let credential = PhoneAuthProvider.provider().credential(
            withVerificationID: verificationID,
            verificationCode: code
        )
        Auth.auth().signIn(with: credential) { authResult, error in
            if let error = error {
                onResult(nil, error.localizedDescription)
                return
            }
            self.phoneVerificationId = nil
            onResult(Self.map(authResult?.user, provider: "phone"), nil)
        }
    }

    // MARK: - Helpers

    private static func map(_ user: User?, provider: String) -> IosAuthUser? {
        guard let user = user else { return nil }
        return IosAuthUser(
            uid: user.uid,
            provider: provider,
            fullName: user.displayName,
            email: user.email,
            phoneNumber: user.phoneNumber,
            photoUrl: user.photoURL?.absoluteString
        )
    }

    private static func rootViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene
        return scene?.windows.first(where: { $0.isKeyWindow })?.rootViewController
    }

    /// Firebase talab qiladigan tasodifiy nonce.
    private static func randomNonceString(length: Int = 32) -> String {
        let charset: [Character] = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        var remaining = length
        while remaining > 0 {
            var random: UInt8 = 0
            let status = SecRandomCopyBytes(kSecRandomDefault, 1, &random)
            if status == errSecSuccess {
                if random < UInt8(charset.count) {
                    result.append(charset[Int(random)])
                    remaining -= 1
                }
            }
        }
        return result
    }

    private static func sha256(_ input: String) -> String {
        let hashed = SHA256.hash(data: Data(input.utf8))
        return hashed.map { String(format: "%02x", $0) }.joined()
    }
}

// MARK: - Apple delegate

extension SocialAuthBridge: ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding, ASWebAuthenticationPresentationContextProviding {

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene
        return scene?.windows.first(where: { $0.isKeyWindow }) ?? ASPresentationAnchor()
    }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene
        return scene?.windows.first(where: { $0.isKeyWindow }) ?? ASPresentationAnchor()
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        let completion = appleCompletion
        appleCompletion = nil

        guard
            let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let nonce = currentNonce,
            let tokenData = appleIDCredential.identityToken,
            let idToken = String(data: tokenData, encoding: .utf8)
        else {
            completion?(nil, "Apple token olinmadi")
            return
        }

        let credential = OAuthProvider.appleCredential(
            withIDToken: idToken,
            rawNonce: nonce,
            fullName: appleIDCredential.fullName
        )
        Auth.auth().signIn(with: credential) { authResult, error in
            if let error = error {
                completion?(nil, error.localizedDescription)
                return
            }
            // Apple faqat birinchi marta ism beradi — uni Firebase profilига yozamiz
            if let fullName = appleIDCredential.fullName,
               let user = authResult?.user,
               (user.displayName?.isEmpty ?? true) {
                let name = [fullName.givenName, fullName.familyName]
                    .compactMap { $0 }
                    .joined(separator: " ")
                if !name.isEmpty {
                    let changeRequest = user.createProfileChangeRequest()
                    changeRequest.displayName = name
                    changeRequest.commitChanges { _ in
                        completion?(Self.map(user, provider: "apple"), nil)
                    }
                    return
                }
            }
            completion?(Self.map(authResult?.user, provider: "apple"), nil)
        }
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        let completion = appleCompletion
        appleCompletion = nil
        let nsError = error as NSError
        if nsError.code == ASAuthorizationError.canceled.rawValue {
            completion?(nil, nil) // Cancelled
        } else {
            completion?(nil, error.localizedDescription)
        }
    }
}
