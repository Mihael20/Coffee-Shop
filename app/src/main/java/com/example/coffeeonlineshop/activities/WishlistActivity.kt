package com.example.coffeeonlineshop.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeonlineshop.adapters.PopularAdapter
import com.example.coffeeonlineshop.databinding.ActivityWishlistBinding
import com.example.coffeeonlineshop.domain.ItemsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { finish() }

        binding.clearAllBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Wishlist")
                .setMessage("Are you sure you want to clear all favorites?")
                .setPositiveButton("Yes") { _, _ ->
                    clearWishlist()
                    loadWishlist()
                }
                .setNegativeButton("No", null)
                .show()
        }

        loadWishlist()
    }

    private fun loadWishlist() {
        val items = getWishlist()
        if (items.isEmpty()) {
            binding.emptyTxt.visibility = View.VISIBLE
            binding.wishlistView.visibility = View.GONE
            binding.clearAllBtn.visibility = View.GONE
        } else {
            binding.emptyTxt.visibility = View.GONE
            binding.wishlistView.visibility = View.VISIBLE
            binding.clearAllBtn.visibility = View.VISIBLE
            binding.wishlistView.layoutManager = GridLayoutManager(this, 2)
            binding.wishlistView.adapter = PopularAdapter(items = items.toMutableList())
        }
    }

    private fun clearWishlist() {
        getSharedPreferences("wishlist", MODE_PRIVATE)
            .edit().remove("items").apply()
    }

    companion object {
        fun getWishlist(context: android.content.Context): MutableList<ItemsModel> {
            val prefs = context.getSharedPreferences("wishlist", MODE_PRIVATE)
            val json = prefs.getString("items", null) ?: return mutableListOf()
            val type = object : TypeToken<MutableList<ItemsModel>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun saveWishlist(context: android.content.Context, items: MutableList<ItemsModel>) {
            val prefs = context.getSharedPreferences("wishlist", MODE_PRIVATE)
            prefs.edit().putString("items", Gson().toJson(items)).apply()
        }

        fun toggleWishlist(context: android.content.Context, item: ItemsModel): Boolean {
            val items = getWishlist(context)
            val exists = items.any { it.title == item.title }
            if (exists) {
                items.removeAll { it.title == item.title }
            } else {
                items.add(item)
            }
            saveWishlist(context, items)
            return !exists
        }

        fun isInWishlist(context: android.content.Context, item: ItemsModel): Boolean {
            return getWishlist(context).any { it.title == item.title }
        }
    }

    private fun getWishlist(): MutableList<ItemsModel> {
        return getWishlist(this)
    }
}