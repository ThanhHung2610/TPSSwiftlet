package com.example.tpsembedding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.tpsembedding.data.PreferencesTPS
import com.example.tpsembedding.HouseManager.WebApi
import com.example.tpsembedding.data.DataManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class LoginActivity : ComponentActivity() {
    private lateinit var usernameEt:TextInputEditText
    private lateinit var passwordEt:TextInputEditText

    private lateinit var errorTv:TextView
    private lateinit var autoLoginCb:CheckBox
    private lateinit var loginBt:Button

    private var autoLoginChecked = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        autoLoginCb = findViewById(R.id.autoLoginCb)
        sharedPreferences = getSharedPreferences(PreferencesTPS.prefFileName, Context.MODE_PRIVATE)
        val autoLogin = sharedPreferences.getBoolean(PreferencesTPS.autoLogin,false)
        autoLoginChecked = sharedPreferences.getBoolean(PreferencesTPS.autoLoginChecked,false)
        if (autoLogin){
            autoLoginCb.isChecked = true
            val username = sharedPreferences.getString(PreferencesTPS.username,"").toString()
            val password = sharedPreferences.getString(PreferencesTPS.password,"").toString()
            login(username,password)
        }
        if (autoLoginChecked) autoLoginCb.isChecked = true
        else autoLoginCb.isChecked = false
        autoLoginCb.setOnClickListener{ onCbAutoLoginClick() }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        usernameEt = findViewById(R.id.inputUsernameLogin)
        passwordEt = findViewById(R.id.inputPasswordLogin)
        errorTv = findViewById(R.id.errorLoginTv)
        usernameEt.setText(sharedPreferences.getString(PreferencesTPS.username,""))

        loginBt = findViewById(R.id.LoginBt)
        loginBt.setOnClickListener { onLoginClick() }
    }

    @SuppressLint("CommitPrefEdits")
    private fun onCbAutoLoginClick(){
        autoLoginChecked = !autoLoginChecked
        val editor = sharedPreferences.edit()
        editor.putBoolean(PreferencesTPS.autoLoginChecked, autoLoginChecked)
        editor.apply()
    }
    private fun onLoginClick(){
        val username_value = usernameEt.text.toString()
        val passw_value = passwordEt.text.toString()
        login(username_value,passw_value)
    }
    private fun login(username:String,password:String){
        CoroutineScope(Dispatchers.IO).launch {
            val jsonData = "{\"username\":\"$username\",\"password\":\"$password\"}"
            val url = WebApi.getLoginUrl()
            val response = Utils.makePostRequest(url,jsonData)
            if (response != null) {
                val jsonElement:JsonElement
                withContext(Dispatchers.Default){
                    jsonElement = Json.parseToJsonElement(response)
                }
                withContext(Dispatchers.Main){
                    val jsonObject = jsonElement.jsonObject
                    val isLoginSuccess = jsonObject["isSuccessful"]?.jsonPrimitive?.boolean
                    if (isLoginSuccess == true){
                        onLoginSuccess(username,password)
                    }
                    else {
                        errorTv.text = jsonObject["error"]?.jsonPrimitive?.content
                    }
                }
            }else {
                withContext(Dispatchers.Main) { errorTv.text = resources.getString(R.string.connection_error_webapi) }
            }
        }
    }
    private fun onLoginSuccess(username:String,password:String){
        DataManager.username = username
        DataManager.password = password
        val editor = sharedPreferences.edit()
        editor.putString(PreferencesTPS.username, username)
        if (autoLoginCb.isChecked){
            editor.putBoolean(PreferencesTPS.autoLogin,true)
            editor.putString(PreferencesTPS.password,password)
        }
        else {
            editor.putBoolean(PreferencesTPS.autoLogin,false)
            editor.remove(PreferencesTPS.password)
        }
        editor.apply()  // Save the changes
        // Direct to main activity
        direct2Main()
    }
    private fun direct2Main(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        // Close login activity
        finish()
    }
}

