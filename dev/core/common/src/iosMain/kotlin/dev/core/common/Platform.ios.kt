package dev.core.common

import platform.UIKit.UIDevice

actual val platformName: String =
    UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

actual val deviceName: String = UIDevice.currentDevice.name
