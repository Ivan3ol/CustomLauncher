package com.ivanzolotarovcustomlauncher.model

import com.ivanzolotarovcustomlauncher.utils.CITY_NAMES

//Repository class to deal with WeatherAPI
class WeatherRepository(private val weatherAPI: WeatherAPI){

    suspend fun fetchAllCities(): ArrayList<WeatherAPI.Response> {
        val responses = ArrayList<WeatherAPI.Response>()
        for(city in CITY_NAMES) {
            //Use lowercase() as all cities names are lowercase in API URLs
            responses.add(weatherAPI.getCityInfo(city.lowercase()))
        }
        return responses
    }

    suspend fun fetchCityByName(city: String): WeatherAPI.Response {
        return weatherAPI.getCityInfo(city.lowercase())
    }
}