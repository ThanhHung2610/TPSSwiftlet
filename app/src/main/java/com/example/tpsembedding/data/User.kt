package com.example.tpsembedding.data

import kotlinx.serialization.Serializable

@Serializable
data class User (var id:Long?=null, var email:String?=null,
                 var full_name:String?=null, var phone:String?=null,
                 var role:String?=null, var username:String,
                 var password:String?=null, var is_deleted:Boolean?=null)