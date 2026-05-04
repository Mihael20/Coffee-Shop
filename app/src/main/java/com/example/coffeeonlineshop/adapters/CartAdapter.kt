package com.example.coffeeonlineshop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.coffeeonlineshop.Helper.ChangeNumberItemsListener
import com.example.coffeeonlineshop.Helper.ManagmentCart
import com.example.coffeeonlineshop.databinding.ViewholderCartBinding
import com.example.coffeeonlineshop.domain.ItemsModel

class CartAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    private val context: Context,
    var changeNumberItemsListener: ChangeNumberItemsListener? = null
) : RecyclerView.Adapter<CartAdapter.Viewholder>() {

    private val managmentCart = ManagmentCart(context)

    class Viewholder(val binding: ViewholderCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = listItemSelected[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.feeEachItem.text = "$${item.price}"
        holder.binding.totalEachItem.text = "$${item.numberInCart * item.price}"
        holder.binding.numberInCartTxt.text = item.numberInCart.toString()

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .apply(RequestOptions().transform(CenterCrop()))
            .into(holder.binding.picCart)

        holder.binding.plusBtn.setOnClickListener {
            managmentCart.plusItem(listItemSelected, holder.bindingAdapterPosition,
                object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyDataSetChanged()
                        changeNumberItemsListener?.onChanged()
                    }
                })
        }

        holder.binding.minusBtn.setOnClickListener {
            managmentCart.minusItem(listItemSelected, holder.bindingAdapterPosition,
                object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyDataSetChanged()
                        changeNumberItemsListener?.onChanged()
                    }
                })
        }

        // fixed: deleteBtn → removeItemBtn to match XML
        holder.binding.removeItemBtn.setOnClickListener {
            managmentCart.removeItem(listItemSelected, holder.bindingAdapterPosition,
                object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyDataSetChanged()
                        changeNumberItemsListener?.onChanged()
                    }
                })
        }
    }

    override fun getItemCount(): Int = listItemSelected.size
}