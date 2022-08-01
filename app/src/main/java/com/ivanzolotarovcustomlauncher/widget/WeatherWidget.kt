package com.ivanzolotarovcustomlauncher.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import com.ivanzolotarovcustomlauncher.R
import com.ivanzolotarovcustomlauncher.model.WeatherAPI
import com.ivanzolotarovcustomlauncher.model.data.WeatherInfo
import com.ivanzolotarovcustomlauncher.model.WeatherRepository
import com.ivanzolotarovcustomlauncher.services.GridWidgetService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

//Widget provider class
class WeatherWidget : AppWidgetProvider() {
    private var observer: WeatherContentObserver? = null

    //Method that is called whenever widget has to be updated
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        //If data observer is not instantiated yet, then create and register it
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

            //Using Dispatchers.IO fetch data from external API and save it to content provider
            CoroutineScope(Dispatchers.IO).launch {
                val list = WeatherRepository(WeatherAPI()).fetchAllCities()
                val resolver = context.contentResolver
                for(response in list){
                    if (response.responseCode== HttpURLConnection.HTTP_OK){
                        //If everything is ok, then save actual weather information to the provider
                        resolver.update(WeatherContentProvider.CONTENT_URI, ContentValues().apply {
                            put(WeatherContentProvider.Fields.CITY,response.data.cityName.toString())
                            put(WeatherContentProvider.Fields.REGION,response.data.regionName.toString())
                            put(WeatherContentProvider.Fields.TEMPERATURE,response.data.temperature)
                            put(WeatherContentProvider.Fields.DESCRIPTION,response.data.description.toString())
                        },null,null)
                    }else{
                        //If an error occurred, then save information about it to the provider
                        resolver.update(WeatherContentProvider.CONTENT_URI, ContentValues().apply {
                            put(WeatherContentProvider.Fields.CITY,response.data.cityName.toString())
                            put(WeatherContentProvider.Fields.REGION, WeatherInfo.REGION_ERROR_VALUE)
                            put(WeatherContentProvider.Fields.TEMPERATURE,0)
                            put(WeatherContentProvider.Fields.DESCRIPTION,response.errorMessage)
                        },null,null)
                    }
                }
                //Notify observer about the changes
                context.contentResolver.notifyChange(WeatherContentProvider.CONTENT_URI, null)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    //Method that is called when widget is firstly added to the screen
    override fun onEnabled(context: Context) {
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
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.grid_view)
    }
}