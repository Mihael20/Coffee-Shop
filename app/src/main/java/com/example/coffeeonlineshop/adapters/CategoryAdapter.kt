package com.example.coffeeonlineshop.adapters

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeonlineshop.R
import com.example.coffeeonlineshop.activities.ItemsListActivity
import com.example.coffeeonlineshop.databinding.ViewholderCategoryBinding
import com.example.coffeeonlineshop.domain.CategoryModel

class CategoryAdapter(val items: MutableList<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var selectedPosition = -1
    private var lastSelectedPosition = -1
    private val translationCache = mutableMapOf<String, String>()

    class ViewHolder(val binding: ViewholderCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderCategoryBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val lang = context.resources.configuration.locales[0].language
        if (lang == "mk") {
            val cached = translationCache[item.title]
            if (cached != null) {
                holder.binding.titleCat.text = cached
            } else {
                holder.binding.titleCat.text = item.title
                translateText(item.title) { translated ->
                    translationCache[item.title] = translated
                    holder.binding.titleCat.text = translated
                }
            }
        } else {
            holder.binding.titleCat.text = item.title
        }

        holder.binding.root.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition == RecyclerView.NO_ID.toInt()) return@setOnClickListener

            lastSelectedPosition = selectedPosition
            selectedPosition = adapterPosition

            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)

            Handler(Looper.getMainLooper()).postDelayed({
                val currentItem = items[adapterPosition]
                val intent = Intent(context, ItemsListActivity::class.java).apply {
                    putExtra("title", currentItem.title)
                    putExtra("id", currentItem.id.toString())
                }
                ContextCompat.startActivity(context, intent, null)
            }, 500)
        }

        if (selectedPosition == position) {
            holder.binding.titleCat.setBackgroundResource(R.drawable.brown_full_corner_bg)
        } else {
            holder.binding.titleCat.setBackgroundResource(R.drawable.brown_2_full_corner)
        }
    }

    private fun translateText(text: String, callback: (String) -> Unit) {
        Thread {
            try {
                Thread.sleep(200)
                val encoded = java.net.URLEncoder.encode(text, "UTF-8")
                val url = java.net.URL("https://api.mymemory.translated.net/get?q=$encoded&langpair=en|mk")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val response = connection.inputStream.bufferedReader().readText()
                val json = org.json.JSONObject(response)
                val translated = json
                    .getJSONObject("responseData")
                    .getString("translatedText")

                Handler(Looper.getMainLooper()).post {
                    callback(translated)
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback(text)
                }
            }
        }.start()
    }

    override fun getItemCount(): Int = items.size
}