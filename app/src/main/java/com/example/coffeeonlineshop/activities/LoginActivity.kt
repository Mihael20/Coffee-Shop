package com.example.coffeeonlineshop.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeonlineshop.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign in failed: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private val facebookSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        callbackManager.onActivityResult(result.resultCode, result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setupGoogle()
        setupFacebook()
        setupButtons()
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.coffeeonlineshop.R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken

                    // Земи Facebook слика со access token
                    val photoUrl = "https://graph.facebook.com/${token.userId}/picture?type=large&access_token=${token.token}"

                    val credential = FacebookAuthProvider.getCredential(token.token)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            // Зачувај ја сликата во Firebase профилот
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(photoUrl))
                                .build()
                            authResult.user?.updateProfile(profileUpdates)
                                ?.addOnSuccessListener { goToMain() }
                                ?: goToMain()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@LoginActivity,
                                "Facebook auth failed: ${it.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                }
                override fun onCancel() {
                    Toast.makeText(this@LoginActivity,
                        "Facebook login cancelled", Toast.LENGTH_SHORT).show()
                }
                override fun onError(error: FacebookException) {
                    Toast.makeText(this@LoginActivity,
                        "Facebook error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupButtons() {
        binding.apply {

            loginBtn.setOnClickListener {
                val email = emailEdt.text.toString().trim()
                val password = passwordEdt.text.toString().trim()
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@LoginActivity,
                        "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { goToMain() }
                    .addOnFailureListener { exception ->
                        val message = when {
                            exception.message?.contains("no user record") == true ->
                                "Account not found. Please register first."
                            exception.message?.contains("password is invalid") == true ->
                                "Wrong password. Try again."
                            exception.message?.contains("deleted") == true ->
                                "This account has been deleted."
                            exception.message?.contains("badly formatted") == true ->
                                "Invalid email format."
                            else -> "Login failed: ${exception.message}"
                        }
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
            }

            registerBtn.setOnClickListener {
                val email = emailEdt.text.toString().trim()
                val password = passwordEdt.text.toString().trim()
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@LoginActivity,
                        "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (password.length < 6) {
                    Toast.makeText(this@LoginActivity,
                        "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { goToMain() }
                    .addOnFailureListener { exception ->
                        val message = when {
                            exception.message?.contains("already in use") == true ->
                                "Email already registered. Try logging in."
                            exception.message?.contains("badly formatted") == true ->
                                "Invalid email format."
                            else -> "Register failed: ${exception.message}"
                        }
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
            }

            googleBtn.setOnClickListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }

            facebookBtn.setOnClickListener {
                LoginManager.getInstance().logInWithReadPermissions(
                    this@LoginActivity,
                    callbackManager,
                    listOf("email", "public_profile")
                )
            }

            anonymousBtn.setOnClickListener {
                auth.signInAnonymously()
                    .addOnSuccessListener { goToMain() }
                    .addOnFailureListener {
                        Toast.makeText(this@LoginActivity,
                            "Anonymous login failed: ${it.message}",
                            Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { goToMain() }
            .addOnFailureListener {
                Toast.makeText(this, "Google auth failed: ${it.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}