package com.example.firechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.google.firebase.FirebaseApp
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
        var EMAIL = "email"
        const val TAG = "Login"
        const val FACEBOOK_SIGN_IN = 64206
        const val GOOGLE_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_login)

        //Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1079804979782-biupcn69v8395q4kjvfd46vnqlk4qad3.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        google_login_button.setSize(SignInButton.SIZE_STANDARD)
        google_login_button.setOnClickListener{ signIn() }

        //Facebook
        facebookCallbackManager = CallbackManager.Factory.create()
        facebook_login_button.setReadPermissions(EMAIL, "public_profile")
        facebook_login_button.registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    firebaseAuthWithFacebook(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    // ...
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    // ...
                }
            })
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and call chat rooms if true.
        val currentUser = authInstance.currentUser
        if (currentUser != null) {
            val chatIntent = Intent(this, ChatroomActivity::class.java)
            startActivity(chatIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Facebook Pass the activity result back to the Facebook SDK
        if (requestCode == FACEBOOK_SIGN_IN) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }

        //Google
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    //Facebook
    private fun firebaseAuthWithFacebook(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)

        authenticateLogin(credential)
    }

    //Google
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        authenticateLogin(credential)
    }

    //TrySignIn
    private fun authenticateLogin(credential: AuthCredential) {
        authInstance.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithCredential:success")
                    val user = authInstance.currentUser
                    if (user != null) {
                        val chatIntent = Intent(this, ChatroomActivity::class.java)
                        startActivity(chatIntent)
                    }

                } else {
                    // If sign in fails, display a Message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                    alert("Sign In failed", "Sign in") {
                        yesButton { }
                    }.show()

                }
            }
    }
}
