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

class PopularAdapter(private val items: MutableList<ItemsModel>) :
    RecyclerView.Adapter<PopularAdapter.Viewholder>() {

    private lateinit var context: Context
    private val translationCache = mutableMapOf<String, String>()

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
        val lang = context.resources.configuration.locales[0].language

        // Title
        if (lang == "mk") {
            val cachedTitle = translationCache[item.title]
            if (cachedTitle != null) {
                holder.binding.titleTxt.text = cachedTitle
            } else {
                holder.binding.titleTxt.text = item.title
                translateText(item.title) { translated ->
                    translationCache[item.title] = translated
                    holder.binding.titleTxt.text = translated
                }
            }
        } else {
            holder.binding.titleTxt.text = item.title
        }

        holder.binding.priceTxt.text = "$${item.price}"

        // Extra
        if (lang == "mk" && item.extra.isNotEmpty()) {
            val cachedExtra = translationCache[item.extra]
            if (cachedExtra != null) {
                holder.binding.subtitleTxt.text = cachedExtra
            } else {
                holder.binding.subtitleTxt.text = "..."
                translateText(item.extra) { translated ->
                    translationCache[item.extra] = translated
                    holder.binding.subtitleTxt.text = translated
                }
            }
        } else {
            holder.binding.subtitleTxt.text = item.extra
        }

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

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(translated)
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    callback(text)
                }
            }
        }.start()
    }

    override fun getItemCount(): Int = items.size
}