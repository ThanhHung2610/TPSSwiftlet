package com.example.tpsembedding.HouseManager

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.tpsembedding.R
import com.example.tpsembedding.Utils
import com.example.tpsembedding.data.DataManager
import com.example.tpsembedding.data.House
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class AddHouseActivity : ComponentActivity() {
    private lateinit var house_nameT:TextInputEditText
    private lateinit var errorTv:TextView
    private lateinit var createBt:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_house)

        house_nameT = findViewById(R.id.inputHouseName)
        createBt = findViewById(R.id.createHouseBt)
        errorTv = findViewById(R.id.errorAddHouseTv)

        createBt.setOnClickListener { onCreateHouse() }
    }

    private fun onCreateHouse(){
        val name = house_nameT.text.toString()
        if (name.length == 0){
            errorTv.setText(resources.getString(R.string.input_error_empty,resources.getString(R.string.house_name)))
            return
        }
        // Send info to web api
        CoroutineScope(Dispatchers.IO).launch{
            val jsonData = "{\"name\":\"$name\"}"
            val url = WebApi.getCreateHouseUrl()
            val response = Utils.makePostRequest(url, jsonData)
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
                        val isSuccess = jsonObject["isSuccessful"]?.jsonPrimitive?.boolean
                        if (isSuccess == true) {
                            val data = jsonObject["data"]?.jsonArray
                            val newHouseId =
                                data?.get(0)?.jsonObject?.get("id")?.jsonPrimitive?.long
                            if (newHouseId != null) {
                                onCreateSuccess(newHouseId, name)
                            }
                        }
                    }
                }
            }else {
                withContext(Dispatchers.Main) { errorTv.text = resources.getString(R.string.connection_error_webapi) }
            }
        }
    }
    private fun onCreateSuccess(houseId:Long,houseName:String){
        // Add House to houses_list
        DataManager.addHouse(House(houseId,houseName, devicesNumber = 0, holesId = mutableListOf()))
        // Turn on flag createHouseSuccess
        DataManager.isCreateHouseSuccess = true
        // Close this activity
        finish()
    }
    private fun redirect2login(){
        Utils.redirect2login(this)
    }
}
