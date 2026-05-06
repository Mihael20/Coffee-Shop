package com.example.coffeeonlineshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeonlineshop.domain.BannerModel
import com.example.coffeeonlineshop.domain.CategoryModel
import com.example.coffeeonlineshop.domain.ItemsModel
import com.google.firebase.database.*

class MainRepository {

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    private fun parseItemsModel(snapshot: DataSnapshot): ItemsModel? {
        return try {
            val title = snapshot.child("title").getValue(String::class.java) ?: ""
            val description = snapshot.child("description").getValue(String::class.java) ?: ""
            val price = snapshot.child("price").getValue(Double::class.java) ?: 0.0
            val rating = snapshot.child("rating").getValue(Double::class.java) ?: 0.0
            val extra = snapshot.child("extra").getValue(String::class.java) ?: ""

            // Чита picUrl и како низа и како објект
            val picUrl = ArrayList<String>()
            val picUrlSnapshot = snapshot.child("picUrl")
            for (pic in picUrlSnapshot.children) {
                val url = pic.getValue(String::class.java)
                if (url != null) picUrl.add(url)
            }

            ItemsModel(
                title = title,
                description = description,
                picUrl = picUrl,
                price = price,
                rating = rating,
                extra = extra
            )
        } catch (e: Exception) {
            null
        }
    }

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()
        val ref: DatabaseReference = firebaseDatabase.getReference("Banner")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(BannerModel::class.java)
                    if (item != null) list.add(item)
                }
                listData.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        return listData
    }

    fun loadItemCategory(categoryId: String): LiveData<MutableList<ItemsModel>> {
        val itemsLiveData = MutableLiveData<MutableList<ItemsModel>>()
        val ref = firebaseDatabase.getReference("Items")
        val query: Query = ref.orderByChild("categoryId").equalTo(categoryId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val item = parseItemsModel(childSnapshot)
                    if (item != null) list.add(item)
                }
                itemsLiveData.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        return itemsLiveData
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref: DatabaseReference = firebaseDatabase.getReference("Category")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    if (item != null) list.add(item)
                }
                listData.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        return listData
    }

    fun loadPopular(): LiveData<MutableList<ItemsModel>> {
        val listData = MutableLiveData<MutableList<ItemsModel>>()
        val ref: DatabaseReference = firebaseDatabase.getReference("Popular")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val item = parseItemsModel(childSnapshot)
                    if (item != null) list.add(item)
                }
                listData.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        return listData
    }
}