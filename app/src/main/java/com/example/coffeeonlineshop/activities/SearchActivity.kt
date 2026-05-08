package com.example.coffeeonlineshop.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeonlineshop.R
import com.example.coffeeonlineshop.adapters.PopularAdapter
import com.example.coffeeonlineshop.databinding.ActivitySearchBinding
import com.example.coffeeonlineshop.viewmodel.MainViewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val query = intent.getStringExtra("query") ?: ""
        val showAll = intent.getBooleanExtra("show_all", false)

        binding.backBtn.setOnClickListener { finish() }

        if (query.isNotEmpty()) {
            saveToHistory(query)
        }

        binding.clearHistoryBtn.setOnClickListener {
            clearHistory()
            binding.historyLayout.visibility = View.GONE
            binding.resultView.visibility = View.GONE
            binding.emptyTxt.visibility = View.VISIBLE
            binding.emptyTxt.text = getString(R.string.history_cleared)
            binding.searchResultTitle.text = getString(R.string.recent_searches)
        }

        if (showAll) {
            binding.searchResultTitle.text = getString(R.string.all_coffees)
            binding.historyLayout.visibility = View.GONE
            loadAll()
        } else if (query.isNotEmpty()) {
            binding.searchResultTitle.text = "${getString(R.string.results_for)}$query"
            binding.historyLayout.visibility = View.GONE

            val lang = resources.configuration.locales[0].language
            if (lang == "mk") {
                binding.progressBar.visibility = View.VISIBLE
                translateToEnglish(query) { englishQuery ->
                    binding.progressBar.visibility = View.GONE
                    search(englishQuery)
                }
            } else {
                search(query)
            }
        } else {
            binding.searchResultTitle.text = getString(R.string.recent_searches)
            showHistory()
        }
    }

    private fun saveToHistory(query: String) {
        val prefs = getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val history = prefs.getStringSet("history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        history.add(query)
        prefs.edit().putStringSet("history", history).apply()
    }

    private fun clearHistory() {
        val prefs = getSharedPreferences("search_history", Context.MODE_PRIVATE)
        prefs.edit().remove("history").apply()
    }

    private fun getHistory(): List<String> {
        val prefs = getSharedPreferences("search_history", Context.MODE_PRIVATE)
        return prefs.getStringSet("history", emptySet())?.toList() ?: emptyList()
    }

    private fun showHistory() {
        val history = getHistory()
        if (history.isEmpty()) {
            binding.historyLayout.visibility = View.GONE
            binding.emptyTxt.visibility = View.VISIBLE
            binding.emptyTxt.text = getString(R.string.no_recent_searches)
        } else {
            binding.emptyTxt.visibility = View.GONE
            binding.historyLayout.visibility = View.VISIBLE
            binding.historyRecycler.layoutManager = LinearLayoutManager(this)
            binding.historyRecycler.adapter = HistoryAdapter(history) { selected ->
                binding.searchResultTitle.text = "${getString(R.string.results_for)}$selected"
                binding.historyLayout.visibility = View.GONE
                binding.clearHistoryBtn.visibility = View.GONE

                val lang = resources.configuration.locales[0].language
                if (lang == "mk") {
                    binding.progressBar.visibility = View.VISIBLE
                    translateToEnglish(selected) { englishQuery ->
                        binding.progressBar.visibility = View.GONE
                        search(englishQuery)
                    }
                } else {
                    search(selected)
                }
            }
        }
    }

    private fun loadAll() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadPopular().observe(this) { items ->
            binding.progressBar.visibility = View.GONE
            if (items.isEmpty()) {
                binding.emptyTxt.visibility = View.VISIBLE
                binding.resultView.visibility = View.GONE
            } else {
                binding.emptyTxt.visibility = View.GONE
                binding.resultView.visibility = View.VISIBLE
                binding.resultView.layoutManager = GridLayoutManager(this, 2)
                binding.resultView.adapter = PopularAdapter(items = items.toMutableList())
            }
        }
    }

    private fun search(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadPopular().observe(this) { items ->
            binding.progressBar.visibility = View.GONE
            val filtered = items.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }.toMutableList()
            if (filtered.isEmpty()) {
                binding.emptyTxt.visibility = View.VISIBLE
                binding.emptyTxt.text = getString(R.string.no_results)
                binding.resultView.visibility = View.GONE
            } else {
                binding.emptyTxt.visibility = View.GONE
                binding.resultView.visibility = View.VISIBLE
                binding.resultView.layoutManager = GridLayoutManager(this, 2)
                binding.resultView.adapter = PopularAdapter(items = filtered)
            }
        }
    }

    private fun translateToEnglish(text: String, callback: (String) -> Unit) {
        val coffeeDict = mapOf(
            "капучино" to "Cappuccino",
            "капочино" to "Cappuccino",
            "еспресо" to "Espresso",
            "еспрессо" to "Espresso",
            "лате" to "Latte",
            "американо" to "Americano",
            "топла чоколада" to "Hot Chocolate",
            "чоколада" to "Chocolate",
            "матча" to "Matcha",
            "макијато" to "Macchiato",
            "кортадо" to "Cortado",
            "карамел" to "Caramel",
            "лешник" to "Hazelnut",
            "ванила" to "Vanilla",
            "класично" to "Classic",
            "темна" to "Dark",
            "бела" to "White",
            "шлаг" to "Whipped",
            "ладно" to "Iced",
            "уметност" to "Art"
        )

        val lowerText = text.lowercase()
        for ((mk, en) in coffeeDict) {
            if (lowerText.contains(mk)) {
                callback(en)
                return
            }
        }

        Thread {
            try {
                val encoded = java.net.URLEncoder.encode(text, "UTF-8")
                val url = java.net.URL("https://api.mymemory.translated.net/get?q=$encoded&langpair=mk|en")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val response = connection.inputStream.bufferedReader().readText()
                val json = org.json.JSONObject(response)
                val translated = json
                    .getJSONObject("responseData")
                    .getString("translatedText")

                runOnUiThread {
                    callback(translated)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    callback(text)
                }
            }
        }.start()
    }

    inner class HistoryAdapter(
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txt: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            view.findViewById<TextView>(android.R.id.text1).setTextColor(
                resources.getColor(R.color.white, null)
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txt.text = "🔍 ${items[position]}"
            holder.itemView.setOnClickListener { onClick(items[position]) }
        }

        override fun getItemCount() = items.size
    }
}