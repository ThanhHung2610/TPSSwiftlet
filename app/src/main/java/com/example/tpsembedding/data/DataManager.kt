package com.example.tpsembedding.data

import kotlinx.serialization.json.Json

object DataManager {
    var isAddingDevice: Boolean = false
    var isConnectingDevice: Boolean = false
    var ssid: String? = null
    var pass: String? = null
    var deviceName: String? = null
    var locationX: String? = null
    var locationY: String? = null
    var isSendInfoSuccess: Boolean = false
    var deviceId:Long?=null
    var houses:ArrayList<House> = ArrayList()
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
    var holesId:MutableList<Long> = mutableListOf()
    var isCreateHouseSuccess:Boolean = false
    var isAddDeviceSuccess:Boolean = false
    var hole1Id:Int?=null
    var hole2Id:Int?=null
    var username:String?=null
    var password:String? = null

    const val MAX_TIME_PAUSE:Long= 300000L
    fun clear(){
        ssid = null
        pass = null
        locationX = null
        locationY = null
        deviceName = null
        deviceId = null
        hole1Id=null
        hole2Id=null
        username=null
        password = null
    }

    fun getCurrentHouse(position: Int):House{
        return houses[position]
    }
    fun getCurrentHousePosition(houseId:Long):Int{
        return houses.indexOfFirst{ house -> house.id == houseId }
    }
    fun addHouse(house:House){
        // Check whether house was in houses
        if (!houses.any { it.id == house.id })
            // Add house to houses
            houses.add(house)
    }
}