package com.example.firechat.data

import com.google.firebase.auth.FirebaseAuth

val authInstance: FirebaseAuth = FirebaseHelper.authinstance()

//Share auth instance through the application using singleton
class FirebaseHelper() {

    companion object {

        private var authInstance: FirebaseAuth? = null

        @Synchronized
        fun authinstance(): FirebaseAuth {
            if (authInstance == null)
                authInstance = FirebaseAuth.getInstance()

            return authInstance!!
        }
    }
}