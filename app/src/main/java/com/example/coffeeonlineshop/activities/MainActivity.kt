package com.example.coffeeonlineshop.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.coffeeonlineshop.R
import com.example.coffeeonlineshop.adapters.CategoryAdapter
import com.example.coffeeonlineshop.adapters.PopularAdapter
import com.example.coffeeonlineshop.databinding.ActivityMainBinding
import com.example.coffeeonlineshop.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            view.layoutParams.height = resources.getDimensionPixelSize(
                com.google.android.material.R.dimen.m3_bottom_nav_min_height
            ) + systemBars.bottom
            insets
        }

        loadUserInfo()
        initBanner()
        initCategory()
        initPopular()
        initBottomMenu()
    }

    private fun loadUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val name = when {
                user.isAnonymous -> "Guest"
                !user.displayName.isNullOrEmpty() -> user.displayName!!
                !user.email.isNullOrEmpty() -> user.email!!.substringBefore("@")
                else -> "User"
            }
            binding.userNameTxt.text = name

            val photoUrl = user.photoUrl?.toString()
            binding.userProfileImg?.let { imageView ->
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(imageView)
                } else {
                    // Facebook слика
                    val fbUid = user.providerData
                        .find { it.providerId == "facebook.com" }?.uid
                    if (fbUid != null) {
                        val fbPhotoUrl = "https://graph.facebook.com/$fbUid/picture?type=large"
                        Glide.with(this)
                            .load(fbPhotoUrl)
                            .transform(CircleCrop())
                            .placeholder(R.drawable.profile)
                            .into(imageView)
                    } else {
                        Glide.with(this)
                            .load(R.drawable.profile)
                            .transform(CircleCrop())
                            .into(imageView)
                    }
                }
            }
        }
    }

    private fun initBottomMenu() {
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun initPopular() {
        binding.apply {
            progressBarPopular.visibility = View.VISIBLE
            viewModel.loadPopular().observeForever {
                popularView.layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL, false
                )
                popularView.adapter = PopularAdapter(items = it)
                progressBarPopular.visibility = View.GONE
            }
        }
    }

    private fun initCategory() {
        binding.apply {
            progressCategory.visibility = View.VISIBLE
            viewModel.loadCategory().observe(this@MainActivity, Observer { list ->
                categoryView.layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                categoryView.adapter = CategoryAdapter(items = list)
                progressCategory.visibility = View.GONE
            })
        }
    }

    private fun initBanner() {
        binding.apply {
            progressBarBanner.visibility = View.VISIBLE
            viewModel.loadBanner().observe(this@MainActivity, Observer { list ->
                if (!list.isNullOrEmpty()) {
                    Glide.with(this@MainActivity)
                        .load(list[0].url)
                        .into(banner)
                }
                progressBarBanner.visibility = View.GONE
            })
        }
    }
}