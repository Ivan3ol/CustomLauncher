package com.ivanzolotarovcustomlauncher.widget

import android.content.Context
import android.database.Cursor
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.ivanzolotarovcustomlauncher.R
import com.ivanzolotarovcustomlauncher.model.data.WeatherInfo

class GridRemoteViewsFactory(private val context: Context): RemoteViewsService.RemoteViewsFactory {
    private var mCursor: Cursor? = null

    //Has to be overridden
    override fun onCreate() {}

    //Called whenever notified about data set changed
    override fun onDataSetChanged() {
        //Close previous cursor if existed
        mCursor?.close()
        //Query content provider for new data
        mCursor = context.contentResolver.query(WeatherContentProvider.CONTENT_URI, null,null,null)
    }

    override fun onDestroy() {
        //Close cursor
        mCursor?.close()
    }

    override fun getCount(): Int {
        return if(mCursor==null) 0 else mCursor?.count!!
    }

    override fun getViewAt(p0: Int): RemoteViews {
        //Set default values if any error occur
        var cityName = WeatherInfo.NAME_ERROR_VALUE
        var region = WeatherInfo.REGION_ERROR_VALUE
        var temperature = "-"
        var description = WeatherInfo.DESCRIPTION_ERROR_VALUE

        if(mCursor?.moveToPosition(p0) == true){
            //Get city name from cursor with null checks
            cityName = mCursor?.getColumnIndex(WeatherContentProvider.Fields.CITY)
                ?.let { mCursor?.getString(it) }.toString()
            //Get region name from cursor with null checks
            region = mCursor?.getColumnIndex(WeatherContentProvider.Fields.REGION)
                ?.let { mCursor?.getString(it) }.toString()
            //Check whether it is actual weather information or error description
            if(region!= WeatherInfo.REGION_ERROR_VALUE){
                //If it is actual weather information then fetch temperature
                val temperatureInt = mCursor?.getColumnIndex(WeatherContentProvider.Fields.TEMPERATURE)
                    ?.let { mCursor?.getInt(it) }
                //Add "+" to temperature string if it is bigger than 0
                temperature = if(temperatureInt!=null&&temperatureInt>0){
                    "+$temperatureInt"
                }else{
                    temperatureInt.toString()
                }
            }
            description = mCursor?.getColumnIndex(WeatherContentProvider.Fields.DESCRIPTION)
                ?.let { mCursor?.getString(it) }.toString()
        }
        //Update UI accordingly to fetched information
        return RemoteViews(context.packageName, R.layout.single_city_forecast).apply {
            setTextViewText(R.id.city_title_tv, cityName)
            setTextViewText(R.id.city_temperature_tv, temperature)
            setTextViewText(R.id.city_region_tv, region)
            setTextViewText(R.id.city_description_tv, description)
        }
    }

    //Create a view that should be shown while getViewAt() is being processed
    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.weather_widget).apply {
            setTextViewText(R.id.city_title_tv, "Loading")
            setTextViewText(R.id.city_temperature_tv, "")
            setTextViewText(R.id.city_region_tv, "")
            setTextViewText(R.id.city_description_tv, "")
        }
    }

    //There is only 1 type of view that will be shown
    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
