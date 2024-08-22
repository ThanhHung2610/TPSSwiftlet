package com.example.tpsembedding.HouseManager

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

object WebApi {
    const val UNAUTHENTICATED_MESSAGE  = "Unauthorized"
    //private const val address = "http://10.0.0.19:44409"
    private const val address = "https://swiftlet.tps-technology.vn"
    private const val login_url = "api/auth/login"
    fun getLoginUrl():String{
        return "$address/$login_url"
    }
    private const val logout_url = "api/auth/logout"
    fun getLogoutUrl():String{
        return "$address/$logout_url"
    }
    private const val createHouse_url = "api/house/create"
    fun getCreateHouseUrl():String{
        return "$address/$createHouse_url"
    }
    private const val addDevice_url = "api/house/addBoard"
    fun getAddDeviceUrl():String{
        return "$address/$addDevice_url"
    }
    private const val getHouses_url = "api/house/all"
    fun getHousesUrl():String{
        return "$address/$getHouses_url"
    }
    private const val recordDaily_url = "api/swiftletRecord/daily"
    fun getRecordDailyUrl(houseId:Long):String{
        return "$address/$recordDaily_url/$houseId"
    }
    private const val record_url = "api/swiftletRecord"
    fun getRecordUrl(houseId:Long):String{
        return "$address/$record_url/$houseId"
    }
    private const val userInfo_url = "api/user/getUserInfo"
    fun getUserInfoUrl():String{
        return "$address/$userInfo_url"
    }
    private const val createAccount_url = "api/user/createAccount"
    fun getCreateAccountUrl():String{
        return "$address/$createAccount_url"
    }
    private const val changePassword_url = "api/user/changePasswordByOwner"
    fun getChangePasswordUrl():String{
        return "$address/$changePassword_url"
    }
    private const val updateUser_url = "api/user/updateByOwner"
    fun getupdateUserUrl():String{
        return "$address/$updateUser_url"
    }
}

class MyCookie:CookieJar{
    private val cookieStore: MutableMap<String, List<Cookie>> = mutableMapOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: listOf()
    }
}