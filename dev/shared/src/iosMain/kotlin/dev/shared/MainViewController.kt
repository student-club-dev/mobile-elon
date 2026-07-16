package dev.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** iOS (SwiftUI) shu funksiyani chaqirib Compose ekranini oladi. */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
