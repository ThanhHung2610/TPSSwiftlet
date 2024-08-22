package com.example.tpsembedding

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tpsembedding.HouseManager.MyCookie
import com.example.tpsembedding.HouseManager.WebApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
object Utils {
    private val client = OkHttpClient.Builder()
        .cookieJar(MyCookie())
        .build()
    fun makeGetRequest(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .build()
        println(request)
        return try {
            val response: Response = client.newCall(request).execute()
            if (response.code == 401)
                WebApi.UNAUTHENTICATED_MESSAGE
            else if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    fun makePostRequest(url:String, jsonData:String):String?{
        val requestBody = jsonData.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        println(request)
        return try {
            val response: Response = client.newCall(request).execute()
            if (response.code == 401)
                WebApi.UNAUTHENTICATED_MESSAGE
            else if (response.isSuccessful){
                response.body?.string()
            }
            else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun checkPermissions(activity: Activity){
        val permissions = mutableListOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
//            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            // Request permissions
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(),
                1
            )
        }
    }
    fun bytearrayToLong(byteArray:ByteArray):Long{
        try {
            val stringValue = byteArray.joinToString("") { it.toInt().toChar().toString() }
            return stringValue.toLong()
        }
        catch (e:NumberFormatException){
            println("bytearrayToLong: $e")
            return (-1).toLong()
        }
    }
    fun isIntNum(input:String):Boolean{
        return try {
            val integerValue = input.toInt()
            // Proceed with the long value
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    fun redirect2login(activity: Activity){
        val intent = Intent(activity,LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}