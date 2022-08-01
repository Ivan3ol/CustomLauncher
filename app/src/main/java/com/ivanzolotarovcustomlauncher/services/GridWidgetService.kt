package com.ivanzolotarovcustomlauncher.services

import android.content.Intent
import android.widget.RemoteViewsService
import com.ivanzolotarovcustomlauncher.widget.GridRemoteViewsFactory

//Service that provides RemoteViewsFactory to adapter
class GridWidgetService: RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return GridRemoteViewsFactory(this.applicationContext)
    }
}