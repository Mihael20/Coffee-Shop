package com.example.coffeeonlineshop.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.coffeeonlineshop.R
import com.example.coffeeonlineshop.databinding.ActivityDetailBinding
import com.example.coffeeonlineshop.domain.ItemsModel
import com.example.coffeeonlineshop.Helper.ManagmentCart

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

        bundle()
        initSizeList()
    }

    private fun initSizeList() {
        binding.apply {
            SmallBtn.setOnClickListener {
                SmallBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                MediumBtn.setBackgroundResource(0)
                LargeBtn.setBackgroundResource(0)
            }
            MediumBtn.setOnClickListener {
                SmallBtn.setBackgroundResource(0)
                MediumBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
                LargeBtn.setBackgroundResource(0)
            }
            LargeBtn.setOnClickListener {
                SmallBtn.setBackgroundResource(0)
                MediumBtn.setBackgroundResource(0)
                LargeBtn.setBackgroundResource(R.drawable.brown_full_corner_bg)
            }
        }
    }

    private fun bundle() {
        binding.apply {
            item = intent.getSerializableExtra("object") as ItemsModel

            Glide.with(this@DetailActivity)
                .load(item.picUrl[0])
                .into(binding.picMain)

            titleTxt.text = item.title
            descriptionTxt.text = item.description
            priceTxt.text = "$" + item.price
            ratingTxt.text = item.rating.toString()

            // Постави срце според wishlist статус
            updateWishlistIcon()

            // Срце копче
            favBtn.setOnClickListener {
                val added = WishlistActivity.toggleWishlist(this@DetailActivity, item)
                updateWishlistIcon()
                if (added) {
                    android.widget.Toast.makeText(
                        this@DetailActivity,
                        "${item.title} added to favourites ❤️",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        this@DetailActivity,
                        "${item.title} removed from favourites",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }

            addToCartBtn.setOnClickListener {
                item.numberInCart = Integer.valueOf(
                    numberInCartTxt.text.toString()
                )
                managmentCart.insertItems(item)
            }

            backBtn.setOnClickListener { finish() }

            plusBtn.setOnClickListener {
                numberInCartTxt.text = (item.numberInCart + 1).toString()
                item.numberInCart++
            }

            minusBtn.setOnClickListener {
                if (item.numberInCart > 0) {
                    numberInCartTxt.text = (item.numberInCart - 1).toString()
                    item.numberInCart--
                }
            }
        }
    }

    private fun updateWishlistIcon() {
        if (WishlistActivity.isInWishlist(this, item)) {
            binding.favBtn.setImageResource(R.drawable.ic_heart_filled)
        } else {
            binding.favBtn.setImageResource(R.drawable.ic_heart_outline)
        }
    }
}