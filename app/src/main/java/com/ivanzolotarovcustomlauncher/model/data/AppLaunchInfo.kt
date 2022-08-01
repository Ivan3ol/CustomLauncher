package com.ivanzolotarovcustomlauncher.model.data

import android.graphics.drawable.Drawable

//Data class for App Drawer RecyclerView
data class AppLaunchInfo(
    var label: CharSequence,
    var packageName: CharSequence,
    var icon: Drawable
)