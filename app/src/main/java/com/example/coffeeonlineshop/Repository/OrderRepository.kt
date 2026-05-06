package com.example.coffeeonlineshop.Repository

import com.example.coffeeonlineshop.domain.ItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveOrder(items: List<ItemsModel>, totalPrice: Double) {
        val userId = auth.currentUser?.uid ?: return

        val order = hashMapOf(
            "userId" to userId,
            "items" to items.map { item ->
                hashMapOf(
                    "title" to item.title,
                    "price" to item.price,
                    "quantity" to item.numberInCart,
                    "total" to item.price * item.numberInCart
                )
            },
            "totalPrice" to totalPrice,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "status" to "pending"
        )

        db.collection("orders")
            .add(order)
            .addOnSuccessListener {
                android.util.Log.d("Firestore", "Order saved: ${it.id}")
            }
            .addOnFailureListener {
                android.util.Log.e("Firestore", "Error: ${it.message}")
            }
    }

    fun getOrders(onSuccess: (List<Map<String, Any>>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        android.util.Log.d("Firestore", "Getting orders for userId: $userId")

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                android.util.Log.d("Firestore", "Orders count: ${result.size()}")
                val orders = result.documents.mapNotNull { it.data }
                onSuccess(orders)
            }
            .addOnFailureListener {
                android.util.Log.e("Firestore", "Error getting orders: ${it.message}")
                onSuccess(emptyList())
            }
    }
}