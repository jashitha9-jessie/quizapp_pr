package com.example.quizapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.databinding.ActivityLoginBinding
import com.example.quizapp.util.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken == null) {
                    showLoading(false)
                    toast("Google sign-in returned no ID token")
                } else {
                    firebaseAuthWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                showLoading(false)
                toast("Google sign-in failed (code ${e.statusCode})")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnGoogleSignIn.setOnClickListener {
            showLoading(true)
            signInLauncher.launch(GoogleAuthHelper.client(this).signInIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Already signed in? Skip the login screen.
        if (auth.currentUser != null) goToHome()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            showLoading(false)
            if (task.isSuccessful) {
                goToHome()
            } else {
                toast("Firebase sign-in failed: ${task.exception?.localizedMessage}")
            }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun showLoading(loading: Boolean) {
        binding.progressLogin.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnGoogleSignIn.isEnabled = !loading
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
