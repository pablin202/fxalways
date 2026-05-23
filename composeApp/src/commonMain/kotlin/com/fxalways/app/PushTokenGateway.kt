package com.fxalways.app

expect object PushTokenGateway {
    suspend fun registerForUser(uid: String?)
}
