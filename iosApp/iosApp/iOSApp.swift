import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Koin DI ni iOS tomonida ishga tushirish. Auth to'liq backendда
        // (`/v1/auth/business/…`), shuning uchun boshqa SDK sozlash kerak emas.
        KoinIosKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}
