package com.example.tpsembedding.data

import kotlinx.serialization.Serializable

@Serializable
data class House(val id: Long,
                 val name:String,
                 var devicesNumber:Int,
                 val holesId:MutableList<Long>)
