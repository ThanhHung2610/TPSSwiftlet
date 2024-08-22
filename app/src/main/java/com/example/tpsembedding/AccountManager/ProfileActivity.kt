package com.example.tpsembedding.AccountManager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tpsembedding.HouseManager.WebApi
import com.example.tpsembedding.LoginActivity
import com.example.tpsembedding.R
import com.example.tpsembedding.Utils
import com.example.tpsembedding.data.DataManager
import com.example.tpsembedding.data.PreferencesTPS
import com.example.tpsembedding.data.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class ProfileActivity : AppCompatActivity() {
    private lateinit var usernameT:TextInputEditText
    private lateinit var fullnameT:TextInputEditText
    private lateinit var emailT:TextInputEditText
    private lateinit var phoneT:TextInputEditText
    private lateinit var updateBt:Button
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var updateSw:Switch
    private var user: User? = null
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        sharedPreferences = getSharedPreferences(PreferencesTPS.prefFileName, Context.MODE_PRIVATE)

        usernameT = findViewById(R.id.usernameProfile)
        fullnameT = findViewById(R.id.fullnameProfile)
        emailT = findViewById(R.id.emailProfile)
        phoneT = findViewById(R.id.phoneProfile)
        updateBt = findViewById(R.id.updateProfileBt)
        updateBt.setOnClickListener { onUpdateProfile() }
        updateSw = findViewById(R.id.updateProfileSw)
        updateSw.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                enableFieldToUpdate()
            else {
                updateUIWithUser(user)
                disableFieldToUpdate()
            }
        }
        DataManager.username?.let { requestUserInfo(it) }
        disableFieldToUpdate()
    }

    private fun onUpdateProfile(){
        val username = usernameT.text.toString()
        val fullname = fullnameT.text.toString()
        val email = emailT.text.toString()
        val phone = phoneT.text.toString()
        user = User(username=username, full_name = fullname,
                        email = email, phone = phone)
        CoroutineScope(Dispatchers.IO).launch {
            val url = WebApi.getupdateUserUrl()
            val jsonData = DataManager.json.encodeToString(user)
            val response = Utils.makePostRequest(url, jsonData)
            response?.let {
                if (it == WebApi.UNAUTHENTICATED_MESSAGE){
                    withContext(Dispatchers.Main) {
                        redirect2login()
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        updateErrorTv(R.string.inform_successful, R.color.forest_green)
                    }
                }
            }?: run {
                withContext(Dispatchers.Main) {
                    updateErrorTv(R.string.connection_error_webapi,R.color.red)
                }
            }
        }
    }
    private fun updateUIWithUser(user:User?){
        usernameT.setText(user?.username)
        fullnameT.setText(user?.full_name)
        emailT.setText(user?.email)
        phoneT.setText(user?.phone)
    }
    private fun requestUserInfo(username:String){
        CoroutineScope(Dispatchers.IO).launch {
            val url = WebApi.getUserInfoUrl()
            val response = Utils.makePostRequest(url, jsonData = "{\"username\":\"$username\"}")
            response?.let {
                if (it == WebApi.UNAUTHENTICATED_MESSAGE){
                    withContext(Dispatchers.Main) {
                        redirect2login()
                    }
                }
                else {
                    withContext(Dispatchers.Default) {
                        val jsonElement = Json.parseToJsonElement(response)
                        val data = jsonElement.jsonObject["data"]?.jsonObject
                        user = data?.toString()
                            ?.let { it1 -> DataManager.json.decodeFromString<User>(it1) }
                    }
                    withContext(Dispatchers.Main) {
                        // update userinfo to UI
                        updateUIWithUser(user)
                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    updateErrorTv(R.string.connection_error_webapi, R.color.red)
                }
            }
        }
    }
    private fun updateErrorTv(errorId:Int, colorId:Int){
        val errorTv = findViewById<TextView>(R.id.errorUpdateProfileTv)
        errorTv.text = resources.getString(errorId)
        errorTv.setTextColor(ContextCompat.getColor(this,colorId))
    }
    private fun disableFieldToUpdate(){
        fullnameT.isEnabled = false
        emailT.isEnabled = false
        phoneT.isEnabled = false
        updateBt.isEnabled = false
    }
    private fun enableFieldToUpdate(){
        fullnameT.isEnabled = true
        emailT.isEnabled = true
        phoneT.isEnabled = true
        updateBt.isEnabled = true
    }
    private fun redirect2login(){
        Utils.redirect2login(this)
    }

}