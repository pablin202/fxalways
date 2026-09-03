package com.fxalways.app

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual object LocalDataReset {
    actual fun clearAll() {
        val defaults = NSUserDefaults.standardUserDefaults
        val domain = NSBundle.mainBundle.bundleIdentifier ?: return
        defaults.removePersistentDomainForName(domain)
        defaults.synchronize()
    }
}
