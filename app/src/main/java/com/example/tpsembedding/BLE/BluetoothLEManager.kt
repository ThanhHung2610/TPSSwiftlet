package com.example.tpsembedding.BLE

import android.app.Activity
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.tpsembedding.showToast
import java.util.UUID

object BluetoothLEManager {
    var bluetoothGatt: BluetoothGatt? = null
    private val UART_SERVICE_UUID = java.util.UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E") // Replace with your service UUID
    private val UART_RX_CHARACTERISTIC_UUID = java.util.UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E") // To send message
    val UART_TX_CHARACTERISTIC_UUID = java.util.UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")  // To read notifications

    fun connect(context: Context, device: BluetoothDevice?, callback: BluetoothGattCallback) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        //context.showToast("Connecting to ${device?.name}")
        bluetoothGatt = device?.connectGatt(context, false, callback)
    }
    fun disconnectGatt(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun writeCharacteristic(context: Context, message: String): Boolean {
        bluetoothGatt?.let { gatt ->
            val service = gatt.getService(UART_SERVICE_UUID)
            val characteristic = service?.getCharacteristic(UART_RX_CHARACTERISTIC_UUID)
            if (characteristic != null) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (context is AppCompatActivity) {
                        context.runOnUiThread { context.showToast("BLUETOOTH_CONNECT is not permitted") }
                    }
                    return false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    gatt.writeCharacteristic(characteristic, message.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                }else{
                    characteristic.value = message.toByteArray()
                    gatt.writeCharacteristic(characteristic)
                }
                return true
            } else {
                if (context is AppCompatActivity) {
                    context.runOnUiThread { context.showToast("characteristic is not found") }
                }
                return false
            }
        } ?: run{
            return false
        }
    }

    fun readCharacteritstic(context: Context){
        bluetoothGatt?.let { gatt ->
            val service = gatt.getService(UART_SERVICE_UUID)
            val characteristic = service?.getCharacteristic(UART_TX_CHARACTERISTIC_UUID)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            if (characteristic != null) {
                val yes = gatt.readCharacteristic(characteristic)
                println("did read method $yes")
            }
        }
    }
    fun setupCharacteristicNotification(context: Context) {
        bluetoothGatt?.let { gatt ->
            val service = gatt.getService(UART_SERVICE_UUID)
            if (service == null) {
                println("BLE Service not found: $UART_SERVICE_UUID")
                return
            }
            val characteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID)
            if (characteristic == null) {
                println("BLE Characteristic not found: $UART_TX_CHARACTERISTIC_UUID")
                return
            }
            if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                println("BLE Characteristic does not have notify property.")
                return
            }
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                context.showToast("no permitting bluetooth connect")
                return
            }
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor =
                characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            } else {
                println("BLE Descriptor not found.")
            }
        }
    }

    fun closeBle(context: Context){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}