package com.ivanzolotarovcustomlauncher.ui.appdrawer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ivanzolotarovcustomlauncher.R
import com.ivanzolotarovcustomlauncher.model.data.AppLaunchInfo

//Adapter for Recycler View with apps
class AppDrawerAdapter : RecyclerView.Adapter<AppDrawerAdapter.ViewHolder>() {
    private val appsList: MutableList<AppLaunchInfo>

    // Subclass, which represents one row of the recycler view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView
        var img: ImageView
        //Open application by its package name upon click on its layout
        override fun onClick(v: View) {
            val pos = adapterPosition
            val context: Context = v.context
            val launchIntent: Intent? = context.packageManager.getLaunchIntentForPackage(
                appsList[pos].packageName.toString()
            )
            context.startActivity(launchIntent)
        }

        //Assign view ids in constructor
        init {
            textView = itemView.findViewById(R.id.app_title)
            img = itemView.findViewById(R.id.app_icon) as ImageView
            itemView.setOnClickListener(this)
        }
    }

    //Show information from the list using view components
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val appLabel = appsList[i].label.toString()
        val appIcon = appsList[i].icon
        val textView = viewHolder.textView
        textView.text = appLabel
        val imageView: ImageView = viewHolder.img
        imageView.setImageDrawable(appIcon)
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.app_row, parent, false)
        return ViewHolder(view)
    }

    init {
        appsList = ArrayList()
    }

    //Apps are being added one by one to the adapter
    fun addApp(info: AppLaunchInfo){
        appsList.add(info)
        notifyItemInserted(appsList.size-1)
    }
}