package org.squirrelstash

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

/**
 * Created by Laurent Cornélis on 29/06/2017.
 */
open abstract class SecuredActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val googleApiClient: GoogleApiClient by lazy {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val user: FirebaseUser?
        get() {
            return firebaseAuth.currentUser
        }

    companion object {
        private const val TAG = "SecuredActivity"
        private const val RC_SIGN_IN = 9001
    }


    open fun onUser(user: FirebaseUser?) {}
    open fun onSignOut() {}
    open fun onAuthConnectionError(connectionResult: ConnectionResult) {
        Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG).show()
    }

    open fun onAuthError(exception: Exception) {
        Toast.makeText(this, R.string.authenticationFailed, Toast.LENGTH_LONG).show()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed: ${connectionResult.errorCode} - ${connectionResult.errorMessage}");
        onAuthConnectionError(connectionResult)
    }

    public override fun onStart() {
        super.onStart()

        //onUser(user)

        val optionalPendingResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient)
        if (optionalPendingResult.isDone) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(SecuredActivity.TAG, "Got cached sign-in")
            handleSignInResult(optionalPendingResult.get())
        } else {
            optionalPendingResult.setResultCallback {
                handleSignInResult(it)
            }
        }
    }

    protected fun signIn(): Unit {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, SecuredActivity.RC_SIGN_IN)
    }

    protected fun signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
            firebaseAuth.signOut()
            onUser(user)
            onSignOut()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SecuredActivity.RC_SIGN_IN -> {
                handleSignInResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data))
            }
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d(SecuredActivity.TAG, "handleSignInResult:success = ${result.isSuccess}, status = ${result.status.statusCode} - ${result.status.statusMessage}")
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val account = result.signInAccount!!
            firebaseAuthWithGoogle(account)
        } else {
            signIn()
        }
    }

    private fun firebaseAuthWithGoogle(googleSignInAccount: GoogleSignInAccount) {
        Log.d(SecuredActivity.TAG, "firebaseAuthWithGoogle:" + googleSignInAccount.id!!)

        val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(SecuredActivity.TAG, "signInWithCredential:success")
                        onUser(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(SecuredActivity.TAG, "signInWithCredential:failure", task.exception)
                        task?.exception?.let { onAuthError(it) }
                    }

                })
    }


}