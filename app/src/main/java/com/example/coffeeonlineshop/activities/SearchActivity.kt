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

        // Зачувај во историја
        if (query.isNotEmpty()) {
            saveToHistory(query)
        }

        // Избриши историја — секогаш видливо
        binding.clearHistoryBtn.setOnClickListener {
            clearHistory()
            binding.historyLayout.visibility = View.GONE
            binding.resultView.visibility = View.GONE
            binding.emptyTxt.visibility = View.VISIBLE
            binding.emptyTxt.text = "History cleared ✓"
            binding.searchResultTitle.text = "Recent Searches"
        }

        if (showAll) {
            binding.searchResultTitle.text = "All Coffees"
            binding.historyLayout.visibility = View.GONE
            loadAll()
        } else if (query.isNotEmpty()) {
            binding.searchResultTitle.text = "Results for: $query"
            binding.historyLayout.visibility = View.GONE
            search(query)
        } else {
            binding.searchResultTitle.text = "Recent Searches"
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
            binding.emptyTxt.text = "No recent searches"
        } else {
            binding.emptyTxt.visibility = View.GONE
            binding.historyLayout.visibility = View.VISIBLE
            binding.historyRecycler.layoutManager = LinearLayoutManager(this)
            binding.historyRecycler.adapter = HistoryAdapter(history) { selected ->
                binding.searchResultTitle.text = "Results for: $selected"
                binding.historyLayout.visibility = View.GONE
                binding.clearHistoryBtn.visibility = View.GONE
                search(selected)
            }
        }
    }

    private fun loadAll() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadPopular().observeForever { items ->
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
        viewModel.loadPopular().observeForever { items ->
            binding.progressBar.visibility = View.GONE
            val filtered = items.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }.toMutableList()
            if (filtered.isEmpty()) {
                binding.emptyTxt.visibility = View.VISIBLE
                binding.resultView.visibility = View.GONE
            } else {
                binding.emptyTxt.visibility = View.GONE
                binding.resultView.visibility = View.VISIBLE
                binding.resultView.layoutManager = GridLayoutManager(this, 2)
                binding.resultView.adapter = PopularAdapter(items = filtered)
            }
        }
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
                resources.getColor(com.example.coffeeonlineshop.R.color.white, null)
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