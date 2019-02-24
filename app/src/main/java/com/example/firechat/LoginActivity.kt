package com.example.firechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.firechat.data.authInstance
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton


class LoginActivity : AppCompatActivity() {

    private lateinit var facebookCallbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        const val TOKEN_ID = "1079804979782-biupcn69v8395q4kjvfd46vnqlk4qad3.apps.googleusercontent.com"
        const val EMAIL = "email"
        const val PUBLIC = "public_profile"
        const val TAG = "Login"
        const val FACEBOOK_SIGN_IN = 64206
        const val GOOGLE_SIGN_IN = 1001
        const val FACEBOOK_ONCANCEL = "facebook:onCancel"
        const val FACEBOOK_ONERROR = "facebook:onError"
        const val FACEBOOK_ONSUCCES = "facebook:onSucces: "
        const val FACEBOOK_AUTH = "handleFacebookAccessToken: "
        const val GOOGLE_AUTH = "firebaseAuthWithGoogle: "
        const val AUTH_FAILED = "Authentication failed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_login)

        //Used to handle sign in with google accounts
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(TOKEN_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        google_login_button.setSize(SignInButton.SIZE_STANDARD)
        google_login_button.setOnClickListener{ signIn() }

        //Used to handle sign in with facebook accounts
        facebookCallbackManager = CallbackManager.Factory.create()
        facebook_login_button.setReadPermissions(EMAIL, PUBLIC)
        facebook_login_button.registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, FACEBOOK_ONSUCCES + loginResult)
                    firebaseAuthWithFacebook(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, FACEBOOK_ONCANCEL)
                    // ...
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, FACEBOOK_ONERROR, error)
                    // ...
                }
            })
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and call ChatroomActivity if true.
        val currentUser = authInstance.currentUser
        if (currentUser != null) {
            val chatIntent = Intent(this, ChatroomActivity::class.java)
            startActivity(chatIntent)
        }
    }

    //Handles login from facebook and google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Facebook Pass the activity result back to the Facebook SDK to see login was a success
        if (requestCode == FACEBOOK_SIGN_IN) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }

        //Checks if google sign in succeeded
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, resources.getString(R.string.google_sign_failed), e)
            }
        }
    }

    //called when sign in with googel account is started
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    //Authenticate facebook logins with firebase
    private fun firebaseAuthWithFacebook(token: AccessToken) {
        Log.d(TAG, FACEBOOK_AUTH + token)

        val credential = FacebookAuthProvider.getCredential(token.token)

        authenticateLogin(credential)
    }

    //Authenticate google logins with firebase
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, GOOGLE_AUTH + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        authenticateLogin(credential)
    }

    //Called to authenticate logins with firebase
    private fun authenticateLogin(credential: AuthCredential) {
        authInstance.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, AUTH_FAILED)
                    val user = authInstance.currentUser
                    if (user != null) {
                        val chatIntent = Intent(this, ChatroomActivity::class.java)
                        startActivity(chatIntent)
                    }

                } else {
                    // If sign in fails, display a Message to the user.
                    Log.w(TAG, AUTH_FAILED, task.exception)

                    alert(this.getString(R.string.sign_in_failed), this.getString(R.string.sign_in)) {
                        yesButton { }
                    }.show()

                }
            }
    }
}
