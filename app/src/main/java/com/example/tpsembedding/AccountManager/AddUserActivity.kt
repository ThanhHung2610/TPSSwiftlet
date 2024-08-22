package com.example.tpsembedding.AccountManager

import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.tpsembedding.HouseManager.WebApi
import com.example.tpsembedding.R
import com.example.tpsembedding.Utils
import com.example.tpsembedding.data.DataManager
import com.example.tpsembedding.data.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AddUserActivity : ComponentActivity() {
    private lateinit var usernameT:TextInputEditText
    private lateinit var passwordT:TextInputEditText
    private lateinit var passwordT2:TextInputEditText
    private lateinit var fullnameT:TextInputEditText
    private lateinit var emailT:TextInputEditText
    private lateinit var phoneT:TextInputEditText
    private lateinit var errorTv:TextView
    private lateinit var roleSp:Spinner
    private lateinit var roleAdapter:ArrayAdapter<String>
    private lateinit var createUserBt:Button
    companion object{
        private val roleData = mutableListOf("WORKER")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        usernameT = findViewById(R.id.inputUsernameAdd)
        passwordT = findViewById(R.id.inputPasswordUser)
        passwordT2 = findViewById(R.id.reinputPasswordUser)
        fullnameT = findViewById(R.id.inputFullname)
        emailT = findViewById(R.id.inputEmail)
        phoneT = findViewById(R.id.inputPhone)
        errorTv = findViewById(R.id.errorAddUserTv)
        createUserBt = findViewById(R.id.createUserBt)
        createUserBt.setOnClickListener{ onCreateUser()}
        roleSp = findViewById(R.id.roleSp)
        roleAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,roleData)
        roleSp.adapter = roleAdapter

    }
    private fun vaidationUserInput(username:String,passw:String,passw2:String ):Boolean{
        if (username.isEmpty()){
            errorTv.text = resources.getString(R.string.input_error_empty,
                resources.getString(R.string.username))
            return false
        }
        if (passw.isEmpty()){
            errorTv.text = resources.getString(R.string.input_error_empty,
                resources.getString(R.string.password))
            return false
        }
        else {
            if (passw != passw2){
                errorTv.text = resources.getString(R.string.error_not_match,
                    resources.getString(R.string.password))
                return false
            }
        }
        return true
    }
    private fun onCreateUserSuccess(){
        finish()
    }
    private fun onCreateUser(){
        val username = usernameT.text.toString()
        val passw = passwordT.text.toString()
        val passw2 = passwordT2.text.toString()
        if (!vaidationUserInput(username,passw,passw2))
            return
        val fullname = fullnameT.text.toString()
        val phone = phoneT.text.toString()
        val email = emailT.text.toString()
        val role = roleSp.selectedItem.toString()
        CoroutineScope(Dispatchers.IO).launch {
            val user = User(username = username, full_name = fullname,
                        phone = phone, email = email,
                        password = passw, role = role)
            val url = WebApi.getCreateAccountUrl()
            val jsonData = DataManager.json.encodeToString(user)
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
                            onCreateUserSuccess()
                        } else {
                            errorTv.text = jsonObject["error"]?.jsonPrimitive?.content
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
