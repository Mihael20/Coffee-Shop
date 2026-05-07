package com.example.coffeeonlineshop.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeonlineshop.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class OrderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTxt: TextView
    private lateinit var deleteSelectedBtn: TextView
    private lateinit var deleteAllBtn: TextView
    private lateinit var selectAllBtn: TextView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var orderAdapter: OrderAdapter? = null
    private val orderIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        recyclerView = findViewById(R.id.orderRecyclerView)
        emptyTxt = findViewById(R.id.emptyTxt)
        deleteSelectedBtn = findViewById(R.id.deleteSelectedBtn)
        deleteAllBtn = findViewById(R.id.deleteAllBtn)
        selectAllBtn = findViewById(R.id.selectAllBtn)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<View>(R.id.backBtn).setOnClickListener { finish() }

        selectAllBtn.setOnClickListener {
            orderAdapter?.selectAll()
        }

        deleteSelectedBtn.setOnClickListener {
            val selected = orderAdapter?.getSelectedIds() ?: return@setOnClickListener
            if (selected.isEmpty()) return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_selected))
                .setMessage(getString(R.string.delete_selected_confirm))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> deleteOrders(selected) }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }

        deleteAllBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_all))
                .setMessage(getString(R.string.delete_all_confirm))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> deleteOrders(orderIds.toList()) }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }

        loadOrders()
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return
        orderIds.clear()

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val orders = mutableListOf<Map<String, Any>>()
                result.documents.forEach { doc ->
                    doc.data?.let {
                        orders.add(it)
                        orderIds.add(doc.id)
                    }
                }
                if (orders.isEmpty()) {
                    emptyTxt.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    deleteSelectedBtn.visibility = View.GONE
                    deleteAllBtn.visibility = View.GONE
                    selectAllBtn.visibility = View.GONE
                } else {
                    emptyTxt.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    deleteSelectedBtn.visibility = View.VISIBLE
                    deleteAllBtn.visibility = View.VISIBLE
                    selectAllBtn.visibility = View.VISIBLE
                    orderAdapter = OrderAdapter(orders, orderIds)
                    recyclerView.adapter = orderAdapter
                }
            }
    }

    private fun deleteOrders(ids: List<String>) {
        val batch = db.batch()
        ids.forEach { id ->
            batch.delete(db.collection("orders").document(id))
        }
        batch.commit().addOnSuccessListener { loadOrders() }
    }

    private fun updateStatus(id: String, status: String) {
        db.collection("orders").document(id)
            .update("status", status)
            .addOnSuccessListener { loadOrders() }
    }

    inner class OrderAdapter(
        private val orders: List<Map<String, Any>>,
        private val ids: List<String>
    ) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

        private val selectedIds = mutableSetOf<String>()

        fun getSelectedIds() = selectedIds.toList()

        fun selectAll() {
            if (selectedIds.size == ids.size) selectedIds.clear()
            else selectedIds.addAll(ids)
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val totalTxt: TextView = view.findViewById(R.id.orderTotalTxt)
            val statusTxt: TextView = view.findViewById(R.id.orderStatusTxt)
            val itemsTxt: TextView = view.findViewById(R.id.orderItemsTxt)
            val dateTxt: TextView = view.findViewById(R.id.orderDateTxt)
            val checkBox: CheckBox = view.findViewById(R.id.orderCheckBox)
            val approvedBtn: TextView = view.findViewById(R.id.approvedBtn)
            val canceledBtn: TextView = view.findViewById(R.id.canceledBtn)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_order, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            val id = ids[position]

            holder.totalTxt.text = "${getString(R.string.total_price)}$${order["totalPrice"]}"

            val status = order["status"] as? String ?: "pending"
            val statusMk = when (status) {
                "approved" -> getString(R.string.status_approved)
                "canceled" -> getString(R.string.status_canceled)
                else -> getString(R.string.status_pending)
            }
            holder.statusTxt.text = "${getString(R.string.status)}$statusMk"

            when (status) {
                "approved" -> holder.statusTxt.setTextColor(
                    android.graphics.Color.parseColor("#4CAF50"))
                "canceled" -> holder.statusTxt.setTextColor(
                    android.graphics.Color.parseColor("#F44336"))
                else -> holder.statusTxt.setTextColor(android.graphics.Color.WHITE)
            }

            val timestamp = order["timestamp"] as? Timestamp
            if (timestamp != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                holder.dateTxt.text = "${getString(R.string.date)}${sdf.format(timestamp.toDate())}"
            } else {
                holder.dateTxt.text = ""
            }

            val items = order["items"] as? List<Map<String, Any>> ?: emptyList()
            holder.itemsTxt.text = items.joinToString("\n") {
                "${it["title"]} x${it["quantity"]} - $${it["total"]}"
            }

            holder.checkBox.isChecked = selectedIds.contains(id)
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedIds.add(id)
                else selectedIds.remove(id)
            }

            holder.approvedBtn.text = getString(R.string.approve)
            holder.canceledBtn.text = getString(R.string.cancel_order)

            holder.approvedBtn.setOnClickListener {
                updateStatus(id, "approved")
            }

            holder.canceledBtn.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle(getString(R.string.cancel_order))
                    .setMessage(getString(R.string.cancel_order_confirm))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        updateStatus(id, "canceled")
                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            }
        }

        override fun getItemCount() = orders.size
    }
}