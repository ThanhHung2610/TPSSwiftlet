package com.example.tpsembedding.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel:ViewModel() {
    val currentHouseId = MutableLiveData<Long>()
    val noDataAvailable = MutableLiveData<Boolean>()
}