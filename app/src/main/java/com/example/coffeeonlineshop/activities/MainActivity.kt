package com.example.coffeeonlineshop.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coffeeonlineshop.adapters.CategoryAdapter
import com.example.coffeeonlineshop.adapters.PopularAdapter
import com.example.coffeeonlineshop.databinding.ActivityMainBinding
import com.example.coffeeonlineshop.viewmodel.MainViewModel
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // This automatically handles all phone navigation bar heights
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            // also update height dynamically
            view.layoutParams.height = resources.getDimensionPixelSize(
                com.google.android.material.R.dimen.m3_bottom_nav_min_height
            ) + systemBars.bottom
            insets
        }

        initBanner()
        initCategory()
        initPopular()
    }

    private fun initPopular() {
        binding.apply {
            progressBarPopular.visibility = View.VISIBLE
            viewModel.loadPopular().observeForever{
                popularView.layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL, false)
                popularView.adapter = PopularAdapter(items = it)
                progressBarPopular.visibility = View.GONE

            }
            viewModel.loadPopular()
        }
    }

    private fun initCategory() {
        binding.apply {

            // ✅ FIX: correct id
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