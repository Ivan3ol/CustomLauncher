package com.ivanzolotarovcustomlauncher.widget

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.ivanzolotarovcustomlauncher.model.data.WeatherInfo
import com.ivanzolotarovcustomlauncher.utils.APP_TAG

//ContentProvider used as saving data point between WeatherRepository and GridFactory
class WeatherContentProvider : ContentProvider() {
    companion object {
        //Uri identifying provider
        val CONTENT_URI: Uri = Uri.parse("content://com.ivanzolotarovcustomlauncher.widget.WeatherContentProvider")
    }
    val data = ArrayList<WeatherInfo>()

    //Fields (columns) used by MatrixCursor and ContentValues
    object Fields {
        const val CITY = "city"
        const val TEMPERATURE = "temperature"
        const val REGION = "region"
        const val DESCRIPTION = "description"
        const val UPDATE_TIME = "update_time"
    }

    //Required to be overridden
    override fun onCreate(): Boolean {
        return true
    }

    //Returns all saved data, specific queries are not needed by the widget
    override fun query(p0: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor {
        Log.d(APP_TAG, "Provider query0")
        val c = MatrixCursor(arrayOf(
            Fields.CITY,
            Fields.REGION,
            Fields.TEMPERATURE,
            Fields.DESCRIPTION,
            Fields.UPDATE_TIME
        ))

        for (i in data.indices) {
            val data: WeatherInfo = data[i]
            c.addRow(arrayOf(data.cityName,data.regionName,data.temperature,data.description,data.updateTime))
        }
        return c
    }

    //Required to be overridden
    override fun getType(p0: Uri): String {
        return "vnd.android.cursor.dir/weather"
    }

    //Initial insertions are controlled by update() method
    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    //Widget does not need to delete data
    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    //Updates existing data or adds new
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        if(p1!=null){
            Log.d(APP_TAG, "Provider insert0")
            //Create data object from passed values
            val info = WeatherInfo(p1.getAsString(Fields.CITY),
                p1.getAsString(Fields.REGION),
                p1.getAsInteger(Fields.TEMPERATURE),
                p1.getAsString(Fields.DESCRIPTION),
                p1.getAsLong(Fields.UPDATE_TIME))

            //Look for the saved weather information for the city with the same name,
            //as it is a unique identifier
            for(i in 0 until data.size){
                if(data[i].cityName==p1.getAsString(Fields.CITY)){
                    //When found such information then reassign it and return
                    data[i]=info
                    return 1
                }
            }
            //If there is no such information already saved then add it to the end of the list
            data.add(info)
        }
        return 1
    }
}