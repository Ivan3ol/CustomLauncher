package com.ivanzolotarovcustomlauncher.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import com.ivanzolotarovcustomlauncher.R
import com.ivanzolotarovcustomlauncher.model.WeatherAPI
import com.ivanzolotarovcustomlauncher.model.WeatherRepository
import com.ivanzolotarovcustomlauncher.model.data.WeatherInfo
import com.ivanzolotarovcustomlauncher.services.GridWidgetService
import com.ivanzolotarovcustomlauncher.utils.APP_TAG
import com.ivanzolotarovcustomlauncher.utils.CITY_NAMES
import com.ivanzolotarovcustomlauncher.utils.MILLIS_IN_HOUR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.util.*

//Widget provider class
class WeatherWidget : AppWidgetProvider() {
    private var observer: WeatherContentObserver? = null
    private var weatherRepository = WeatherRepository(WeatherAPI())

    //Method that is called whenever widget has to be updated
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        //If data observer is not instantiated yet, then create and register it
        Log.d(APP_TAG,"Widget update0")
        instantiateObserver(context)

        // There is only one widget in the current program, but there may be more
        for (appWidgetId in appWidgetIds) {

            val intent = Intent(context, GridWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val views = RemoteViews(context.packageName, R.layout.weather_widget).apply {
                //Set adapter using intent to service, which provides instance of RemoteViewsFactory
                setRemoteAdapter(R.id.grid_view, intent)
                //Set view that will be displayed if there is no data to display yet
                setEmptyView(R.id.grid_view, R.id.empty_view)
            }
            //Bind remote views with this widget ID
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(APP_TAG,"Widget update1")

            //Using Dispatchers.IO fetch data from external API and save it to content provider
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(APP_TAG,"Widget update1.5")
                val citiesToUpdate = getCitiesToUpdate(context.contentResolver)

                //Update data for cities, which need an update
                for(city in citiesToUpdate){
                    val response = weatherRepository.fetchCityByName(city)
                    if (response.responseCode== HttpURLConnection.HTTP_OK){
                        //If everything is ok, then save actual weather information to the provider
                       context.contentResolver.update(WeatherContentProvider.CONTENT_URI, ContentValues().apply {
                            put(WeatherContentProvider.Fields.CITY,response.data.cityName.toString())
                            put(WeatherContentProvider.Fields.REGION,response.data.regionName.toString())
                            put(WeatherContentProvider.Fields.TEMPERATURE,response.data.temperature)
                            put(WeatherContentProvider.Fields.DESCRIPTION,response.data.description.toString())
                            put(WeatherContentProvider.Fields.UPDATE_TIME,response.data.updateTime)
                        },null,null)
                    }else{
                        //If an error occurred, then save information about it to the provider
                        context.contentResolver.update(WeatherContentProvider.CONTENT_URI, ContentValues().apply {
                            put(WeatherContentProvider.Fields.CITY,response.data.cityName.toString())
                            put(WeatherContentProvider.Fields.REGION, WeatherInfo.REGION_ERROR_VALUE)
                            put(WeatherContentProvider.Fields.TEMPERATURE,0)
                            put(WeatherContentProvider.Fields.DESCRIPTION,response.errorMessage)
                            put(WeatherContentProvider.Fields.UPDATE_TIME,response.data.updateTime)
                        },null,null)
                    }
                }
                Log.d(APP_TAG,"Widget update2")

            }.invokeOnCompletion {
                //Notify observer about the changes
                context.contentResolver.notifyChange(WeatherContentProvider.CONTENT_URI, null)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    //Method that returns list of cities needing an update
    private fun getCitiesToUpdate(resolver: ContentResolver): MutableList<String> {
        //Get all locally saved data
        val cursorWeather = resolver.query(WeatherContentProvider.CONTENT_URI, null,null,null)
        //Fetch list of all cities, so that if that city data has not been loaded from API yet, it will be loaded now
        val citiesToUpdate = CITY_NAMES.toMutableList()
        //Iterate through saved data
        while (cursorWeather?.moveToNext() == true) {
            val cityName = cursorWeather.getColumnIndex(WeatherContentProvider.Fields.CITY)
                .let { cursorWeather.getString(it) }.toString()
            val updateTime = cursorWeather.getColumnIndex(WeatherContentProvider.Fields.UPDATE_TIME)
                .let { cursorWeather.getLong(it) }

            //Check if data for particular city is outdated (last update more than an hour ago)
            if(updateTime+MILLIS_IN_HOUR>Date().time){
                citiesToUpdate.remove(cityName)
            }
        }
        cursorWeather?.close()
        return citiesToUpdate
    }

    //Method that is called when widget is firstly added to the screen
    override fun onEnabled(context: Context) {
        Log.d(APP_TAG,"Widget enabled0")

        //If data observer is not instantiated yet, then create and register it
        instantiateObserver(context)
    }

    //Function that creates observer, if it does not exist yet
    private fun instantiateObserver(context: Context){
        if(observer==null) {
            observer = WeatherContentObserver(
                AppWidgetManager.getInstance(context),
                ComponentName(context, WeatherWidget::class.java),
                Handler(Looper.getMainLooper())
            )
            context.contentResolver.registerContentObserver(
                WeatherContentProvider.CONTENT_URI, true,
                observer!!
            )
        }
    }

    //Destroy observer when widget is disabled
    override fun onDisabled(context: Context) {
        observer?.let { context.contentResolver.unregisterContentObserver(it) }
        observer = null
    }

}

//Class that observes updates from Content Provider
class WeatherContentObserver(private val mgr: AppWidgetManager, private val cn: ComponentName, h: Handler?) :
    ContentObserver(h) {

    //Notify RemoteViewsFactory about data updates
    override fun onChange(selfChange: Boolean) {
        Log.d(APP_TAG,"Observer change0")
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.grid_view)
    }
}