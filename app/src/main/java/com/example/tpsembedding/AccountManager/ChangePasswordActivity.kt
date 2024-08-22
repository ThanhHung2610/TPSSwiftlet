package com.example.tpsembedding.AccountManager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.tpsembedding.HouseManager.WebApi
import com.example.tpsembedding.LoginActivity
import com.example.tpsembedding.R
import com.example.tpsembedding.Utils
import com.example.tpsembedding.data.DataManager
import com.example.tpsembedding.data.PreferencesTPS
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChangePasswordActivity : ComponentActivity() {
    private lateinit var oldPasswT: TextInputEditText
    private lateinit var newPasswT: TextInputEditText
    private lateinit var newPasswT2: TextInputEditText
    private lateinit var errorTv: TextView
    private lateinit var changeBt: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        oldPasswT = findViewById(R.id.inputOldPassword)
        newPasswT = findViewById(R.id.inputNewPassword)
        newPasswT2 = findViewById(R.id.reinputNewPassword)
        errorTv = findViewById(R.id.errorChangePasswordTv)
        changeBt = findViewById(R.id.changePasswordBt)
        changeBt.setOnClickListener{ onChangePassword()}

    }
    private fun vaidationUserInput(oldPassw:String,newPassw:String,newPassw2:String ):Boolean{
        if (oldPassw.isEmpty() || oldPassw != DataManager.password){
            errorTv.text = resources.getString(
                R.string.wrong_password)
            return false
        }
        if (newPassw.isEmpty()){
            errorTv.text = resources.getString(
                R.string.input_error_empty,
                resources.getString(R.string.password))
            return false
        }
        else {
            if (newPassw != newPassw2){
                errorTv.text = resources.getString(
                    R.string.error_not_match,
                    resources.getString(R.string.password))
                return false
            }
        }
        return true
    }
    private fun onChangeSuccess(passw:String){
        DataManager.password = passw
        val sharedPreferences = getSharedPreferences(PreferencesTPS.prefFileName, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(PreferencesTPS.autoLogin,false)){
            val editor = sharedPreferences.edit()
            editor.putString(PreferencesTPS.password,passw)
            editor.apply()
        }
        finish()
    }
    private fun onChangePassword(){
        val username = oldPasswT.text.toString()
        val passw = newPasswT.text.toString()
        val passw2 = newPasswT2.text.toString()
        if (!vaidationUserInput(username,passw,passw2))
            return
        CoroutineScope(Dispatchers.IO).launch {
            val url = WebApi.getChangePasswordUrl()
            val jsonData = "{\"username\":\"${DataManager.username}\",\"password\":\"$passw\"}"
            val response = Utils.makePostRequest(url, jsonData )
            if (response != null) {
                if (response == WebApi.UNAUTHENTICATED_MESSAGE){
                    withContext(Dispatchers.Main) {
                        redirect2login()
                    }
                }
                else {
                    val jsonElement: JsonElement
                    withContext(Dispatchers.Default) {
                        jsonElement = Json.parseToJsonElement(response)
                    }
                    withContext(Dispatchers.Main) {
                        val jsonObject = jsonElement.jsonObject
                        val isCreateSuccess = jsonObject["isSuccessful"]?.jsonPrimitive?.boolean
                        if (isCreateSuccess == true) {
                            onChangeSuccess(passw)
                        }
                    }
                }
            }else {
                withContext(Dispatchers.Main) { errorTv.text = resources.getString(R.string.connection_error_webapi) }
            }
        }

    }

    private fun redirect2login(){
        Utils.redirect2login(this)
    }
}
