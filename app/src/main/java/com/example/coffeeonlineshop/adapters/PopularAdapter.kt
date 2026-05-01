package com.example.coffeeonlineshop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeonlineshop.databinding.ViewholderPopularBinding
import com.example.coffeeonlineshop.domain.ItemsModel

class PopularAdapter(val items: MutableList<ItemsModel>):
RecyclerView.Adapter<PopularAdapter.Viewholder>() {
lateinit var context: Context

    class Viewholder(val binding: ViewholderPopularBinding):
    RecyclerView.ViewHolder(itemView = binding.root){


    }

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): PopularAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderPopularBinding.inflate(LayoutInflater.from(context), parent, false )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(p0: PopularAdapter.Viewholder, p1: Int) {
       holder.binding.titleTxt.text = items[position].title
        holder.binding.priceTxt.text="$" + items[position].price.toString()
        holder.binding.subtitleTxt.text = items[position].extra

        Glide.with(context)
            .load(model = items[position].picURL[0])
            .into(target = holder.binding.pic)


    }

    override fun getItemCount(): Int = items.size
}