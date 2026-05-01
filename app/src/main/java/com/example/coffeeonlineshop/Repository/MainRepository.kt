package com.example.coffeeonlineshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeonlineshop.domain.BannerModel
import com.example.coffeeonlineshop.domain.CategoryModel
import com.google.firebase.database.*
import java.util.Locale

class MainRepository {

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()

        val ref: DatabaseReference = firebaseDatabase.getReference("Banner")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()

                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(BannerModel::class.java)
                    if (item != null) {
                        list.add(item)
                    }
                }

                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: handle database error
            }
        })

        return listData
    }


    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()

        val ref: DatabaseReference = firebaseDatabase.getReference("Category")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()

                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    if (item != null) {
                        list.add(item)
                    }
                }

                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: handle database error
            }
        })

        return listData
    }
}