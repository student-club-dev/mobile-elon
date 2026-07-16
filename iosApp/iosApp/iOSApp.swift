import SwiftUI
import Shared
import FirebaseCore
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        // Firebase (GoogleService-Info.plist orqali)
        FirebaseApp.configure()

        // Koin DI ni iOS tomonida ishga tushirish
        KoinIosKt.doInitKoin()

        // Kotlin social auth bridge'ni Swift implementatsiyaga ulash
        IosSocialAuthBridge.shared.delegate = SocialAuthBridge()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
                .onOpenURL { url in
                    // Google Sign-In qaytish URL'ini qayta ishlash
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
