package com.ivanzolotarovcustomlauncher.ui.appdrawer

import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import com.ivanzolotarovcustomlauncher.app.LauncherApplication
import com.ivanzolotarovcustomlauncher.base.BaseViewModel
import com.ivanzolotarovcustomlauncher.model.data.AppLaunchInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppDrawerViewModel : BaseViewModel() {
    var adapter: AppDrawerAdapter = AppDrawerAdapter()

    //Start loading apps in constructor, as recycler view is on the screen straightaway after launching the fragment
    init {
        //Fetching might take some time so use coroutine
        viewModelScope.launch(Dispatchers.Default) {
            loadApps()
        }
    }

    private suspend fun loadApps(){
        return withContext(Dispatchers.Default){
            val pm: PackageManager = LauncherApplication.instance.packageManager
            val i = Intent(Intent.ACTION_MAIN, null)
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            val allApps = pm.queryIntentActivities(i, 0)
            for (ri in allApps) {
                val app = AppLaunchInfo(ri.loadLabel(pm),
                    ri.activityInfo.packageName,
                    //Loading icon is the most time-consuming operation among others
                    ri.activityInfo.loadIcon(pm))
                //Add to adapter in main thread as dealing with UI
                launch(Dispatchers.Main){
                    adapter.addApp(app)
                }
            }
        }
    }
}