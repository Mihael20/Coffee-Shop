package com.example.coffeeonlineshop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coffeeonlineshop.Repository.MainRepository
import com.example.coffeeonlineshop.domain.BannerModel

class MainViewModel : ViewModel() {
    private val repository = MainRepository()

    fun loadBanner(): LiveData<MutableList<BannerModel>>{
        return repository.loadBanner()
    }
}