package com.example.coffeeonlineshop.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notifTitle = intent.getStringExtra("notification_title")
        val notifMessage = intent.getStringExtra("notification_message")
        if (!notifTitle.isNullOrEmpty() && !notifMessage.isNullOrEmpty()) {
            android.app.AlertDialog.Builder(this)
                .setTitle(notifTitle)
                .setMessage(notifMessage)
                .setPositiveButton("OK", null)
                .show()
        }

        analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN) {
            param(FirebaseAnalytics.Param.METHOD, "login")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1
            )
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            android.util.Log.d("FCM", "Token: $token")
        }

        showNotification(
            id = 2,
            title = "Coffee Shop",
            message = "Добредојде! Погледни ги нашите нови понуди! ☕"
        )

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

    private fun showNotification(id: Int, title: String, message: String) {
        val channelId = "coffee_shop_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Coffee Shop Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this, id, intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.bell_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }

    private fun loadUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val name = when {
            user.isAnonymous -> "Guest"
            !user.displayName.isNullOrEmpty() -> user.displayName!!
            !user.email.isNullOrEmpty() -> user.email!!.substringBefore("@")
            else -> "User"
        }
        binding.userNameTxt.text = name

        val photoUrl = user.photoUrl?.toString()
        val imageView = binding.userProfileImg ?: return

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(imageView)
        } else {
            val fbUid = user.providerData
                .find { it.providerId == "facebook.com" }?.uid
            if (fbUid != null) {
                Glide.with(this)
                    .load("https://graph.facebook.com/$fbUid/picture?type=large")
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

    private fun initBottomMenu() {
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            analytics.logEvent("profile_opened") {
                param("user_type",
                    if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true)
                        "guest" else "registered")
            }
        }

        binding.bellBtn.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Coffee Shop ☕")
                .setMessage("Нова промоција! Купи едно добиј едно бесплатно!")
                .setPositiveButton("OK", null)
                .show()

            showNotification(
                id = 1,
                title = "Coffee Shop",
                message = "Нова промоција! Купи едно добиј едно бесплатно! ☕"
            )
        }

        binding.myOrderBtn.setOnClickListener {
            startActivity(Intent(this, OrderActivity::class.java))
        }

        binding.wishlistBtn?.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        binding.historyBtn?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.searchEdt?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN) {
                val query = binding.searchEdt?.text.toString().trim()
                if (query.isNotEmpty()) {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra("query", query)
                    startActivity(intent)
                }
                true
            } else false
        }

        binding.textSeeAll.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("show_all", true)
            startActivity(intent)
        }

        binding.filterBtn?.setOnClickListener {
            val query = binding.searchEdt?.text.toString().trim()
            if (query.isNotEmpty()) {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("query", query)
                startActivity(intent)
            } else {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("show_all", true)
                startActivity(intent)
            }
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

                analytics.logEvent("popular_coffees_loaded") {
                    param("count", it.size.toLong())
                }
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