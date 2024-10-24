package com.wisesaint.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.wisesaint.R
import com.wisesaint.databinding.ActivitySigninScreenBinding

class SigninScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivitySigninScreenBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: Initializing SigninScreen")
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("422283407852-f260hcosn5mlhf2qc1khv5em9qo41b13.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up the Google sign-in button click listener
        binding.googleSignInButton.setOnClickListener {
            Log.d(TAG, "Google Sign-In button clicked")

            signIn()
        }

    }

    // Sign-in method
    private fun signIn() {
        Log.d(TAG, "signIn: Starting Google Sign-In process")

        showProgress(true)
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Register the launcher for the Google Sign-In result
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Sign-In result received. ResultCode: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google Sign-In successful. Account ID: ${account.id}")
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.d(TAG, "Google sign-in failed", e)
                showProgress(false)
                Toast.makeText(this, "Google Sign-In Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }

        } else {
            Log.d(TAG, "Google Sign-In was canceled or failed")
            showProgress(false)
            Toast.makeText(this, "Google Sign-In Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    // Authenticate with Firebase using the Google account
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
                showProgress(false)
            }
    }

    // Update UI after successful sign-in
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Log.d(TAG, "updateUI: Signed in as ${user.displayName}")
            Toast.makeText(this, "Signed in as ${user.displayName}", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Log.d(TAG, "updateUI: User is null, sign-in failed")
            Toast.makeText(this, "Please sign in to continue.", Toast.LENGTH_SHORT).show()
        }
    }

    // Show or hide the progress bar
    private fun showProgress(show: Boolean) {
        binding.signinProgress.visibility = if (show) View.VISIBLE else View.GONE
        binding.googleSignInButton.isEnabled = !show
        Log.d(TAG, "Progress bar visibility set to: $show")
    }

    companion object {
        private const val TAG = "SigninScreen"
    }
}