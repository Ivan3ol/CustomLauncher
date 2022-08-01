package com.ivanzolotarovcustomlauncher.ui.home

import androidx.lifecycle.MutableLiveData
import com.ivanzolotarovcustomlauncher.base.BaseViewModel
import com.ivanzolotarovcustomlauncher.services.ChargeListener
import com.ivanzolotarovcustomlauncher.services.TimeListener
import java.text.SimpleDateFormat
import java.util.*


class HomeViewModel : BaseViewModel(), TimeListener, ChargeListener {
    private var timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var currentTime = MutableLiveData<String>()
    var currentCharge = MutableLiveData<Int>()

    //Interface implementation, save value to live data which is observed by view
    override fun setTime(date: Date){
        currentTime.value = timeFormat.format(date)
    }

    fun setCurrentTime(){
        currentTime.value = timeFormat.format(Date())
    }

    //Interface implementation, save value to live data which is observed by view
    override fun setCharge(level: Int) {
        currentCharge.value = level
    }
}