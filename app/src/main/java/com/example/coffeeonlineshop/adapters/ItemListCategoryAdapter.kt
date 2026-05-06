package com.example.coffeeonlineshop.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.coffeeonlineshop.activities.DetailActivity
import com.example.coffeeonlineshop.databinding.ViewholderPopularBinding
import com.example.coffeeonlineshop.domain.ItemsModel

class ItemListCategoryAdapter(private val items: MutableList<ItemsModel>) :
    RecyclerView.Adapter<ItemListCategoryAdapter.Viewholder>() {

    private lateinit var context: Context

    class Viewholder(val binding: ViewholderPopularBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderPopularBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.subtitleTxt.text = item.extra
        holder.binding.priceTxt.text = "$${item.price}"

        val imageUrl = item.picUrl.getOrNull(0)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .into(holder.binding.pic)
        } else {
            holder.binding.pic.setImageDrawable(null)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("object", items[position])
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}