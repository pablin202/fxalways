package com.fxalways.app

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object ExternalUrlOpener {
    actual fun open(url: String) {
        if (url.isBlank()) return
        NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it) }
    }
}
