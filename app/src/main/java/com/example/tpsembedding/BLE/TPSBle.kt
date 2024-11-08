@file:Suppress("DEPRECATION")

package com.example.tpsembedding.BLE

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.tpsembedding.CurrentActivityTracker
import com.example.tpsembedding.data.DataManager
import com.example.tpsembedding.HouseManager.ScanningActivity
import com.example.tpsembedding.Utils
import com.example.tpsembedding.data.SharedViewModel
import com.example.tpsembedding.showToast
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object TPSBle {
    private lateinit var context: Context
    lateinit var sharedViewModel: SharedViewModel
    var bluetoothAdapter:BluetoothAdapter? = null
    var bluetoothLeScanner:BluetoothLeScanner? = null
    var scanning:Boolean = false
    var handler:Handler = Handler(Looper.getMainLooper())

    private const val SCAN_PERIOD = 60000L

    fun initialize(context: Context, sharedViewModel: SharedViewModel){
        this.context = context.applicationContext
        this.sharedViewModel = sharedViewModel
    }

    fun  startScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            context.showToast("no permitting bluetooth scan")
            return
        }
        if (bluetoothAdapter?.isEnabled == true) {
            if (!scanning) {
                scanning = true
                bluetoothLeScanner?.startScan(leScanCallback)
                handler.postDelayed(
                    {stopScan()}, SCAN_PERIOD)
            }
        } else {
            context.showToast("Please enable Bluetooth")
            stopScan()
        }
    }

    fun stopScan(){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            context.showToast("no permitting bluetooth scan")
            return
        }
        if (scanning) {
            bluetoothLeScanner?.stopScan(leScanCallback)
            scanning = false
            // if target device not found
            if (DataManager.isAddingDevice) {
                showToast2Main("${DataManager.deviceName} not found!")
                DataManager.isAddingDevice = false
                // Finish scanning activity
                val currentActivity = CurrentActivityTracker.currentActivity
                if (currentActivity is ScanningActivity) {
                    BluetoothLEManager.closeBle(context)
                    currentActivity.finish()
                }
            }
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            if (!DataManager.isConnectingDevice){
                if (device?.name == DataManager.deviceName){
                    DataManager.isConnectingDevice = true
                    //context.showToast("Found $target_name")
                    connectToDevice(device.address)
                }
            }
        }
    }

    fun connectToDevice(mac_of_device : String?){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val device = bluetoothAdapter?.getRemoteDevice(mac_of_device)
        BluetoothLEManager.connect(context,device,gattCallback())
    }

    fun showToast2Main(message: String){
        handler.post { context.showToast(message)}
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sendMessageToBle(){
        // Send message to this ble
        val wifi = JSONObject()
        wifi.put("ssid", DataManager.ssid)
        wifi.put("password", DataManager.pass)
        wifi.put("houseId", sharedViewModel.currentHouseId.value)
        DataManager.hole1Id?.let {
            wifi.put("hole1Id", it)
            DataManager.hole1Id = null}
        DataManager.hole2Id?.let {
            wifi.put("hole2Id", it)
            DataManager.hole2Id = null }
        val message = wifi.toString()

        val isSuccessful = BluetoothLEManager.writeCharacteristic(context, message)
    }

    private fun gattCallback() = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gatt.discoverServices()
            } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                //showToast2Main("Disconnected")
                println("Disconnected")
                if (!DataManager.isSendInfoSuccess)
                {
                    // Finish scanning activity
                    val currentActivity = CurrentActivityTracker.currentActivity
                    if (currentActivity is ScanningActivity) {
                        BluetoothLEManager.closeBle(context)
                        currentActivity.finish()
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothLEManager.setupCharacteristicNotification(context)
                DataManager.isAddingDevice = false
                stopScan()
                DataManager.isConnectingDevice = false
            } else {
                context.showToast("onServicesDiscovered received: $status")
            }
        }
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == BluetoothLEManager.UART_TX_CHARACTERISTIC_UUID) {
                val data = characteristic.value
                println("hung check data ${data}")
                // Handle the received data as needed
                val customerId = Utils.bytearrayToLong(data)
                if (customerId != (-1).toLong()){
                    DataManager.deviceId = customerId
                    // Send message to this BLE device
                    sendMessageToBle()
                }
                else {
                    val stringValue = data.joinToString("") { it.toInt().toChar().toString()}
                    if (stringValue == "BLE received")
                    {
                        DataManager.isSendInfoSuccess = true
                        BluetoothLEManager.disconnectGatt(context)
                        // Finish scanning activity
                        val currentActivity = CurrentActivityTracker.currentActivity
                        if (currentActivity is ScanningActivity) {
                            currentActivity.finish()
                        }
                    }
                }
            }
        }
    }

    fun release(context:Context){
        bluetoothAdapter = null
        bluetoothLeScanner = null
        if (scanning) stopScan()
    }
}