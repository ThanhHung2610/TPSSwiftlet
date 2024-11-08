package com.example.tpsembedding.HouseManager

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.tpsembedding.BLE.TPSBle
import com.example.tpsembedding.data.DataManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tpsembedding.CurrentActivityTracker
import com.example.tpsembedding.LoginActivity
import com.example.tpsembedding.data.PreferencesTPS
import com.example.tpsembedding.R
import com.example.tpsembedding.Utils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class AddDeviceActivity : ComponentActivity() {
    private lateinit var mpassword: TextInputEditText
    private lateinit var mSSID: TextInputEditText
    private lateinit var mDeviceName: TextInputEditText
    private lateinit var hole1T: TextInputEditText
    private lateinit var hole2T: TextInputEditText

    private lateinit var mnextBt: Button
    private lateinit var errorText: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)


        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        mpassword = findViewById(R.id.inputPasswordWifi)
        mSSID = findViewById(R.id.inputSSID)
        mDeviceName = findViewById(R.id.inputDeviceName)

        hole1T = findViewById(R.id.inputHole1Id)
        hole2T = findViewById(R.id.inputHole2Id)
        errorText = findViewById(R.id.errorAddDeviceTv)

        sharedPreferences = getSharedPreferences(PreferencesTPS.prefFileName, Context.MODE_PRIVATE)
        mSSID.setText(sharedPreferences.getString(PreferencesTPS.ssid,""))
        mpassword.setText(sharedPreferences.getString(PreferencesTPS.passwordWifi,""))
        mDeviceName.setText(sharedPreferences.getString(PreferencesTPS.deviceName,"TPS Swiftlet"))

        mnextBt = findViewById(R.id.NextBt)
        mnextBt.setOnClickListener { onNextClick() }

        Utils.checkPermissions(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Create the LocationRequest object
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(5000)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.let {
                    val location: Location? = it.lastLocation
                    DataManager.locationX = location?.latitude.toString()
                    DataManager.locationY = location?.longitude.toString()
                }
            }
        }
        // Request location updates
        startLocationUpdates()
    }
    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            requestLocationPermission()
        }
    }
    private fun requestLocationPermission() {
        // Request location permission if not granted
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                errorText.text = resources.getString(R.string.notify_no_permit_location)
            }
        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun validationInputData(ssid:String,hole1id:String,hole2id:String):Boolean {
        if (ssid.isEmpty()) {
            errorText.text = resources.getString(
                R.string.input_error_empty,
                resources.getString(R.string.wifi)
            )
            return false
        }
        if (hole1id.isEmpty()) {
            if (hole2id.isEmpty()) {
                errorText.text = resources.getString(
                    R.string.input_error_mustOne,
                    "hole identify"
                )
                return false
            }
        }
        else {
            if (!Utils.isIntNum(hole1id)){
                errorText.text = resources.getString(
                    R.string.input_error_mustbeInt,
                    resources.getString(R.string.hole1Id)
                )
                return false
            }
//            if (DataManager.holesId.contains(hole1id.toLong()))
//            {
//                errorText.text = resources.getString(
//                    R.string.input_error_existed,
//                    hole1id
//                )
//                return false
//            }
        }
        if (hole2id.isNotEmpty()){
            if (!Utils.isIntNum(hole2id)){
                errorText.text = resources.getString(
                    R.string.input_error_mustbeInt,
                    resources.getString(R.string.hole2Id)
                )
                return false
            }
//            if (DataManager.holesId.contains(hole2id.toLong()))
//            {
//                errorText.text = resources.getString(
//                    R.string.input_error_existed,
//                    hole2id
//                )
//                return false
//            }
        }
        return true
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onNextClick(){
        val ssid = mSSID.text.toString()
        val passw = mpassword.text.toString()
        val device_n = mDeviceName.text.toString()
        val hole1id = hole1T.text.toString()
        val hole2id = hole2T.text.toString()

        if (!validationInputData(ssid,hole1id,hole2id))
            return

        DataManager.deviceName = device_n
        DataManager.ssid = ssid
        DataManager.pass = passw
        if (hole1id.isNotEmpty()) DataManager.hole1Id = hole1id.toInt()
        if (hole2id.isNotEmpty()) DataManager.hole2Id = hole2id.toInt()

        val editor = sharedPreferences.edit()
        editor.putString(PreferencesTPS.ssid,ssid)
        editor.putString(PreferencesTPS.passwordWifi,passw)
        editor.putString(PreferencesTPS.deviceName,device_n)
        editor.apply()

        DataManager.isAddingDevice = true
        TPSBle.startScan()
        val intent = Intent(this, ScanningActivity::class.java)
        startActivity(intent)
    }

    private fun addDeviceWebApi(){
        val hole1id = hole1T.text.toString()
        val hole2id = hole2T.text.toString()
        val sensor1id = 1
        val sensor2id = 2
        var response1:String?=null
        var response2:String?=null
        CoroutineScope(Dispatchers.IO).launch {
            val url = WebApi.getAddDeviceUrl()
            if (hole1id.isNotEmpty()){
                val dataObject = buildJsonObject {
                    put("houseId", JsonPrimitive(TPSBle.sharedViewModel.currentHouseId.value))
                    put("holeId", JsonPrimitive(hole1id))
                    put("boardId", JsonPrimitive(DataManager.deviceId))
                    put("sensorId", JsonPrimitive(sensor1id))
                    put("locationX", JsonPrimitive(DataManager.locationX))
                    put("locationY", JsonPrimitive(DataManager.locationY))
                }

                val jsonData = Json.encodeToString(JsonObject.serializer(), dataObject)
                response1 = Utils.makePostRequest(url,jsonData)
            }
            if (hole2id.isNotEmpty()){
                val dataObject = buildJsonObject {
                    put("houseId", JsonPrimitive(TPSBle.sharedViewModel.currentHouseId.value))
                    put("holeId", JsonPrimitive(hole2id))
                    put("boardId", JsonPrimitive(DataManager.deviceId))
                    put("sensorId", JsonPrimitive(sensor2id))
                    put("locationX", JsonPrimitive(DataManager.locationX))
                    put("locationY", JsonPrimitive(DataManager.locationY))
                }

                val jsonData = Json.encodeToString(JsonObject.serializer(), dataObject)
                response2 = Utils.makePostRequest(url,jsonData)
            }

            withContext(Dispatchers.Main){
                // Reset flag isSendInfoSuccess
                DataManager.isSendInfoSuccess = false
                if ((response1 == WebApi.UNAUTHENTICATED_MESSAGE) || (response2 == WebApi.UNAUTHENTICATED_MESSAGE)){
                    withContext(Dispatchers.Main) {
                        redirect2login()
                    }
                }else {
                    if ((response1 == null && hole1id.isNotEmpty()) || (response2 == null && hole2id.isNotEmpty())) {
                        errorText.text = resources.getString(R.string.connection_error_webapi)

                    } else {
                        // Turn on flag isAddDeviceSuccess
                        DataManager.isAddDeviceSuccess = true
                        // Finish the activity
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CurrentActivityTracker.currentActivity = this
    }
    override fun onPause() {
        super.onPause()
        if (CurrentActivityTracker.currentActivity == this) {
            CurrentActivityTracker.currentActivity = null
        }
    }
    override fun onRestart() {
        super.onRestart()
        if(DataManager.isSendInfoSuccess){
            // Send request add device to WebApi
            addDeviceWebApi()
        }
        else{
            errorText.text = resources.getString(R.string.x_not_found,DataManager.deviceName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun redirect2login(){
        Utils.redirect2login(this)
    }

}