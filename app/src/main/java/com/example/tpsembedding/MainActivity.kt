package com.example.tpsembedding

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.tpsembedding.AccountManager.AddUserActivity
import com.example.tpsembedding.AccountManager.ChangePasswordActivity
import com.example.tpsembedding.AccountManager.ProfileActivity

import com.example.tpsembedding.BLE.BluetoothLEManager
import com.example.tpsembedding.BLE.TPSBle
import com.example.tpsembedding.data.DataManager
import com.example.tpsembedding.data.PreferencesTPS
import com.example.tpsembedding.HouseManager.AddDeviceActivity
import com.example.tpsembedding.HouseManager.AddHouseActivity
import com.example.tpsembedding.HouseManager.WebApi
import com.example.tpsembedding.data.House
import com.example.tpsembedding.data.SharedViewModel
import com.example.tpsembedding.data.User
import com.example.tpsembedding.homeTab.ViewPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long


object CurrentActivityTracker {
    @Volatile
    var currentActivity: ComponentActivity? = null
}

class MainActivity : AppCompatActivity() {
    private lateinit var menuNav:Menu

    private lateinit var bluetoothManager: BluetoothManager

    private lateinit var homeTab:TabLayout
    private lateinit var homeViewPager:ViewPager2
    private lateinit var houseSp:Spinner
    private lateinit var houseSpAdapter:ArrayAdapter<String>
    private lateinit var toolbar: Toolbar

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var drawerLayout: DrawerLayout

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Utils.checkPermissions(this)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        houseSp = findViewById(R.id.houseSp)
        val spinnerData = mutableListOf<String>()
        houseSpAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,spinnerData)
        houseSpAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        houseSp.adapter = houseSpAdapter
        houseSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                //val selectedItem = parent.getItemAtPosition(position).toString()
                val currentHouse = DataManager.getCurrentHouse(position)
                sharedViewModel.noDataAvailable.value = currentHouse.devicesNumber == 0
                sharedViewModel.currentHouseId.value = currentHouse.id
                DataManager.holesId = currentHouse.holesId
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle the case where no item is selected
            }
        }

        bluetoothManager = getSystemService(android.bluetooth.BluetoothManager::class.java)
        TPSBle.bluetoothAdapter = bluetoothManager.adapter
        TPSBle.bluetoothLeScanner = TPSBle.bluetoothAdapter?.bluetoothLeScanner
        TPSBle.initialize(this,sharedViewModel)

        if (TPSBle.bluetoothAdapter == null) {
            showToast("Bluetooth not supported")
            finish()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize the navigation drawer
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        menuNav = navigationView.menu
        val logoutButton: View = navigationView.findViewById(R.id.logout_bt)
        logoutButton.setOnClickListener{ onLogout() }
        navigationView.setNavigationItemSelectedListener  {menuItem ->
            when(menuItem.itemId){
                R.id.nav_profile -> {
                    val intent = Intent(this,ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_change_password -> {
                    val intent = Intent(this,ChangePasswordActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_create_user -> {
                    val intent = Intent(this,AddUserActivity::class.java)
                    startActivity(intent)
                }
                // Handle other menu items
            }
            // Close the drawer after handling the menu item
            drawerLayout.closeDrawer(GravityCompat.START)
            true // Return true to indicate the menu item click is handled
        }
        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }


        homeTab = findViewById(R.id.homeTab)
        homeViewPager = findViewById(R.id.home_Vp)
        // Set the adapter for ViewPager2
        homeViewPager.adapter = ViewPagerAdapter(this)
        // Connect TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(homeTab,homeViewPager){
            tab, position -> tab.text = when(position){
                0 -> "Fetch Daily"
                1 -> "Fetch Data"
                else -> null
            }
        }.attach()

        requestData()
    }
    // Handle menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        setOptionalIconsVisible(menu)
        return true
    }
    private fun setOptionalIconsVisible(menu: Menu?) {
        try {
            if (menu != null) {
                val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(menu, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_device_menu -> {
                val intent = Intent(this, AddDeviceActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.add_house_menu -> {
                val intent = Intent(this, AddHouseActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    // Lifecycle function
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
    override fun onDestroy() {
        super.onDestroy()

        BluetoothLEManager.closeBle(this)
        DataManager.clear()
        TPSBle.release(this)
        // Clear currentActivity reference
        if (CurrentActivityTracker.currentActivity == this) {
            CurrentActivityTracker.currentActivity = null
        }
    }
    override fun onRestart() {
        super.onRestart()
        if (DataManager.isCreateHouseSuccess){
            // Get new house name and set to current house
            val houseName = DataManager.houses.last().name
            spAddNewItemAndSetSelection(houseName)
            // Reset flag isCreateHouseSuccess
            DataManager.isCreateHouseSuccess = false
        }
        if (DataManager.isAddDeviceSuccess){
            val currentPosition = houseSp.selectedItemPosition
            DataManager.houses[currentPosition].devicesNumber += 1
            sharedViewModel.noDataAvailable.value = false
            // Reset flag isAddDeviceSuccess
            DataManager.isAddDeviceSuccess = false
        }
    }

    fun requestData(error: Int = 0){
        if (error == 0) {
            requestHouseData()
            DataManager.username?.let { requestUserInfo(it) }
        }
        else if (error == errorConnectionOnRequestingUserInfo)
            DataManager.username?.let { requestUserInfo(it) }
        else if (error == errorConnectionOnRequestingHouseData)
            requestHouseData()
    }
    fun requestUserInfo(username:String){
        CoroutineScope(Dispatchers.IO).launch {
            val url = WebApi.getUserInfoUrl()
            val response = Utils.makePostRequest(url, jsonData = "{\"username\":\"$username\"}")
            response?.let {
                if (it == WebApi.UNAUTHENTICATED_MESSAGE){
                    withContext(Dispatchers.Main) {
                        redirect2login()
                    }
                }else {
                    var user: User?
                    withContext(Dispatchers.Default) {
                        val jsonElement = Json.parseToJsonElement(response)
                        val data = jsonElement.jsonObject["data"]?.jsonObject
                        user = data?.toString()
                            ?.let { it1 -> DataManager.json.decodeFromString<User>(it1) }
                    }
                    withContext(Dispatchers.Main) {
                        // update userinfo to UI
                        val usernameTv = findViewById<TextView>(R.id.usernameTv)
                        usernameTv.text = user?.username
                        val emailTv = findViewById<TextView>(R.id.emailTv)
                        emailTv.text = user?.email
                        // Check role is admin or not
                        // disable some features if role is not admin
                        if (user?.role != "ADMIN") {
                            // Dynamically disable a menu item
                            navMenuDisableItem(R.id.nav_create_user)
                        }
                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    showLostConnectionDialog(1)
                }
            }
        }
    }
    private fun navMenuDisableItem(id:Int){
        val itemToDisable: MenuItem? = menuNav.findItem(id)
        itemToDisable?.isEnabled = false
        itemToDisable?.isVisible = false
    }
    fun requestHouseData(){
        CoroutineScope(Dispatchers.IO).launch {
            val url = WebApi.getHousesUrl()
            val response = Utils.makeGetRequest(url)
            val houses: JsonArray?
            response?.let {
                if (it == WebApi.UNAUTHENTICATED_MESSAGE){
                    withContext(Dispatchers.Main) {
                        redirect2login()
                    }
                }else{
                withContext(Dispatchers.Default){
                    val jsonElement = Json.parseToJsonElement(it)
                    houses = jsonElement.jsonObject["data"]?.jsonArray
                }
                withContext(Dispatchers.Main){
                    if (houses != null) {
                        if (houses.size == 0)
                            sharedViewModel.noDataAvailable.value = true
                        else{
                            for (house in houses){
                                val id = house.jsonObject["id"]?.jsonPrimitive?.long
                                val name = house.jsonObject["name"]?.jsonPrimitive?.content
                                val boards = house.jsonObject["boards"]?.jsonArray
                                var devicesNum = 0
                                var holesId = mutableListOf<Long>()
                                if (boards!=null){
                                    devicesNum = boards.size
                                    for (board in boards){
                                        board.jsonObject["holeId"]?.jsonPrimitive?.long?.let { holeId ->
                                            holesId.add(holeId)
                                        }
                                    }
                                }
                                if (id != null && name != null) {
                                    // update value of spinner
                                    DataManager.addHouse(House(id, name, devicesNum, holesId))
                                    spAddNewItem(name)
                                }
                            }
                            // Handle if changing config (portrait <-> landscape)
                            if (sharedViewModel.currentHouseId.value != null){
                                houseSp.setSelection(DataManager.getCurrentHousePosition(
                                    sharedViewModel.currentHouseId.value!!
                                ))
                            }
                        }
                    }
                }
                    }
            } ?: run {
                withContext(Dispatchers.Main){
                    showLostConnectionDialog(2)
                }
            }
        }
    }
    // Set up dropdown
    private fun spAddNewItem(item:String){
        houseSpAdapter.add(item)
        houseSpAdapter.notifyDataSetChanged()
    }
    private fun spAddNewItemAndSetSelection(item:String){
        spAddNewItem(item)
        houseSp.setSelection(houseSpAdapter.count - 1)
    }

    private fun showLostConnectionDialog(error:Int){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.connection_lost))
        builder.setMessage(resources.getString(R.string.connection_error_webapi))
        builder.setPositiveButton("Retry"){dialog, which ->
            dialog.dismiss()
            requestData(error)
        }
        // Prevent dialog from being dismissed on back button press
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

    private fun onLogout(){
        // Make Login get to web server
        CoroutineScope(Dispatchers.IO).launch {
            val url = WebApi.getLogoutUrl()
            val response = Utils.makeGetRequest(url)
            withContext(Dispatchers.Main){
                if (response != null){
                    onLogoutSuccess()
                }
                else{
                    showToast("Logout failed. Please try again!")
                }
            }
        }
    }
    private fun onLogoutSuccess(){
        val sharedPreferences = getSharedPreferences(PreferencesTPS.prefFileName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PreferencesTPS.autoLogin,false)
        editor.apply()
        // Remove houses list
        DataManager.houses.clear()
        // Redirect to login activity and clear the back stack
        redirect2login()
    }
    private fun redirect2login(){
        Utils.redirect2login(this)
    }
    companion object {
        private const val errorConnectionOnRequestingUserInfo = 1
        private const val errorConnectionOnRequestingHouseData = 2
    }
}