package com.ivanzolotarovcustomlauncher.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

//BroadcastReceiver to get updates on charge level
class ChargeReceiver(private var listener: ChargeListener): BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        p1?.getIntExtra("level",0)?.let { listener.setCharge(it) }
    }
}

//Interface for listener for updates on the charge level
interface ChargeListener{
    fun setCharge(level: Int)
}