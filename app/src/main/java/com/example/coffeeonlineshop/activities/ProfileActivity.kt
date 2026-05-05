package com.example.coffeeonlineshop.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.coffeeonlineshop.R
import com.example.coffeeonlineshop.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    // Image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        loadUserInfo()
        setupButtons()
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        binding.apply {
            if (user != null) {
                when {
                    user.isAnonymous -> {
                        nameTxt.text = "Guest"
                        emailTxt.text = "Anonymous User"
                        Glide.with(this@ProfileActivity)
                            .load(R.drawable.profile)
                            .transform(CircleCrop())
                            .into(profileIcon)
                    }

                    else -> {
                        nameTxt.text = user.displayName
                            ?: user.email?.substringBefore("@")
                                    ?: "User"
                        emailTxt.text = user.email ?: ""

                        // Земи слика — работи за Google и Facebook
                        val photoUrl = user.photoUrl?.toString()
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(photoUrl)
                                .transform(CircleCrop())
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .into(profileIcon)
                        } else {
                            // Facebook слика преку Graph API
                            val fbUid = user.providerData
                                .find { it.providerId == "facebook.com" }?.uid
                            if (fbUid != null) {
                                val fbPhotoUrl =
                                    "https://graph.facebook.com/$fbUid/picture?type=large"
                                Glide.with(this@ProfileActivity)
                                    .load(fbPhotoUrl)
                                    .transform(CircleCrop())
                                    .placeholder(R.drawable.profile)
                                    .into(profileIcon)
                            } else {
                                Glide.with(this@ProfileActivity)
                                    .load(R.drawable.profile)
                                    .transform(CircleCrop())
                                    .into(profileIcon)
                            }
                        }
                    }
                }

                if (!user.isAnonymous) {
                    profileIcon.setOnClickListener {
                        imagePickerLauncher.launch("image/*")
                    }
                }
            }
        }
    }

    private fun uploadProfileImage(uri: Uri) {
        val user = auth.currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()

        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                // Прикажи ја новата слика
                Glide.with(this)
                    .load(uri)
                    .transform(CircleCrop())
                    .into(binding.profileIcon)
                Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupButtons() {
        binding.apply {

            logoutBtn.setOnClickListener {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { _, _ ->
                        auth.signOut()
                        goToLogin()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            deleteAccountBtn.setOnClickListener {
                AlertDialog.Builder(this@ProfileActivity)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure? This cannot be undone!")
                    .setPositiveButton("Delete") { _, _ ->
                        auth.currentUser?.delete()
                            ?.addOnSuccessListener {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Account deleted", Toast.LENGTH_SHORT
                                ).show()
                                goToLogin()
                            }
                            ?.addOnFailureListener {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Failed: ${it.message}", Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            backBtn.setOnClickListener { finish() }
        }
    }

    private fun goToLogin() {
        // Исчисти го Google Sign In
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions
            .Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()
        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn
            .getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Исчисти го Facebook логин
            com.facebook.login.LoginManager.getInstance().logOut()
            // Исчисти го Firebase
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}