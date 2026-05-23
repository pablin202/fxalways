package com.fxalways.app

actual object PushTokenGateway {
    actual suspend fun registerForUser(uid: String?) = Unit
}
