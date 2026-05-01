package com.example.coffeeonlineshop.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.coffeeonlineshop.databinding.ActivityMainBinding
import com.example.coffeeonlineshop.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ FIX 1: Correct inflate usage
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initBanner()
    }

    private fun initBanner() {
        binding.apply {
            progressBarBanner.visibility = View.VISIBLE

            // ✅ FIX 2: Observe properly
            viewModel.loadBanner().observe(this@MainActivity, Observer { list ->

                if (!list.isNullOrEmpty()) {

                    // ✅ FIX 3: Correct Glide syntax
                    Glide.with(this@MainActivity)
                        .load(list[0].url)
                        .into(banner)
                }

                progressBarBanner.visibility = View.GONE
            })
        }
    }
}