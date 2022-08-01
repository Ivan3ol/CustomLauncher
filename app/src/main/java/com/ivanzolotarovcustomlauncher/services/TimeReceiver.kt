package com.ivanzolotarovcustomlauncher.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

//BroadcastReceiver to get updates on time
class TimeReceiver(private var listener: TimeListener): BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        //Call listener on the start of every minute
        if (p1?.action?.compareTo(Intent.ACTION_TIME_TICK) == 0){
            //Pass new Date object as it it contains time of creation
            listener.setTime(Date())
        }
    }
}

//Interface for listener for updates on the time
interface TimeListener{
    fun setTime(date: Date)
}