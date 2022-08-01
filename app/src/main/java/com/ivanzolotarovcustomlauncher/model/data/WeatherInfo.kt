package com.ivanzolotarovcustomlauncher.model.data

//Data class for fetched data from external API and to be shown in a widget
data class WeatherInfo(
    var cityName: CharSequence,
    var regionName: CharSequence,
    var temperature: Int,
    var description: CharSequence,
){
    companion object{
        //Values to be used if error occurred while fetching data
        const val NAME_ERROR_VALUE = "Unknown"
        const val REGION_ERROR_VALUE = "Error"
        const val DESCRIPTION_ERROR_VALUE = "Unknown error"
    }
}