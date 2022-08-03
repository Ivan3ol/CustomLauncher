package com.ivanzolotarovcustomlauncher.model

import android.util.Log
import com.ivanzolotarovcustomlauncher.model.data.WeatherInfo
import com.ivanzolotarovcustomlauncher.utils.APP_TAG
import com.ivanzolotarovcustomlauncher.utils.WEATHER_API_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

//Class to connect to external API
class WeatherAPI {
    //Class with response information
    data class Response(val responseCode: Int,
                        val data: WeatherInfo,
                        val errorMessage: String?)

    suspend fun getCityInfo(cityName: String): Response {
        Log.d(APP_TAG,"request made")
        return withContext(Dispatchers.IO) {
            val connection = createConnection(cityName)
                ?: return@withContext Response(HttpURLConnection.HTTP_GONE,
                    //Capitalising first letter as cityName is currently lowercase
                    WeatherInfo(cityName.replaceFirstChar(Char::titlecase),
                        WeatherInfo.REGION_ERROR_VALUE,0,
                        WeatherInfo.DESCRIPTION_ERROR_VALUE,
                        Date().time),
                    "Connection error")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val info = convertJSON(connection)
                    ?: return@withContext Response(HttpURLConnection.HTTP_GONE,
                        WeatherInfo(cityName.replaceFirstChar(Char::titlecase),
                            WeatherInfo.REGION_ERROR_VALUE,0,
                            WeatherInfo.DESCRIPTION_ERROR_VALUE,
                            Date().time),
                        "Decoding error")
                return@withContext Response(connection.responseCode, info, null)

            } else {
                return@withContext Response(connection.responseCode,
                    WeatherInfo(cityName.replaceFirstChar(Char::titlecase),
                        WeatherInfo.REGION_ERROR_VALUE,0,
                        WeatherInfo.DESCRIPTION_ERROR_VALUE, Date().time),
                    connection.responseMessage)
            }
        }
    }

    private fun createConnection(cityName: String): HttpURLConnection?{
        val connection: HttpURLConnection
        return try {
            //Blocking exception may be ignored as launched using Dispatchers.IO
            val url = URL(WEATHER_API_URL + cityName)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.doInput = true
            connection.doOutput = false
            //trying to establish actual connection by invoking getResponseCode()
            connection.responseCode
            connection
        }catch (e: Exception){
            null
        }
    }

    private fun convertJSON(connection: HttpURLConnection): WeatherInfo?{
        return try {
            val response =
                JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            WeatherInfo(
                response.getString("city"),
                response.getString("country"),
                response.getInt("temperature"),
                response.getString("description"),
                Date().time
            )
        }catch (e: Exception){
            null
        }
    }
}


