package com.ivanzolotarovcustomlauncher.ui.home

import android.app.Activity.RESULT_OK
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.ivanzolotarovcustomlauncher.R
import com.ivanzolotarovcustomlauncher.app.LauncherApplication
import com.ivanzolotarovcustomlauncher.base.BaseFragment
import com.ivanzolotarovcustomlauncher.databinding.FragmentHomeBinding
import com.ivanzolotarovcustomlauncher.services.ChargeReceiver
import com.ivanzolotarovcustomlauncher.services.TimeReceiver
import com.ivanzolotarovcustomlauncher.widget.WeatherWidget
import com.ivanzolotarovcustomlauncher.ui.activity.MainActivity


class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    private lateinit var timeReceiver: TimeReceiver
    private lateinit var chargeReceiver: ChargeReceiver
    private lateinit var clockTextView: TextView
    private lateinit var chargeTextView: TextView
    private lateinit var chargeProgressBar: ProgressBar
    private lateinit var mainContainer: RelativeLayout
    private lateinit var widgetManager: AppWidgetManager
    private lateinit var widgetHost: AppWidgetHost
    private lateinit var addWidgetButton: Button

    //Callback for user decision to allow or not to bind a widget
    private val bindCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK && it.data?.extras!=null) {
            //If allowed then hide respective button and show the widget
            addWidgetButton.visibility = View.GONE
            showWidget(it.data!!.extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID))
        }else{
            //If prohibited show respective toast
            Toast.makeText(context,"Refused",Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = HomeViewModel()
        mBinding.viewModel = mViewModel

        widgetManager = AppWidgetManager.getInstance(activity?.applicationContext)
        //Here 1 is hardcoded hostID, which must be unique in the application
        widgetHost = AppWidgetHost(activity?.applicationContext,1)

        clockTextView = view.findViewById(R.id.clock_tv)
        chargeTextView = view.findViewById(R.id.charge_tv)
        chargeProgressBar = view.findViewById(R.id.charge_pb)
        mainContainer = view.findViewById(R.id.main_container)
        addWidgetButton = view.findViewById(R.id.add_widget_button)

        //If widget is not bound yet, then show respective button
        if(widgetHost.appWidgetIds.isEmpty()){
            addWidgetButton.apply {
                visibility = VISIBLE
                setOnClickListener { bindWidget() }
            }
        }

        view.findViewById<ImageButton>(R.id.icon_drawer).setOnClickListener{
            run {
                (activity as MainActivity).showAppFragment()
            }
        }
    }

    //Method that observes LiveData from viewModel and updates UI respectively
    private fun observeTime() {
        mViewModel.currentTime.observe(viewLifecycleOwner) { time ->
            clockTextView.text = time
        }
    }

    //Method that observes LiveData from viewModel and updates UI respectively
    private fun observeCharge() {
        mViewModel.currentCharge.observe(viewLifecycleOwner) { level ->
            chargeTextView.text = "$level%"
            chargeProgressBar.progress = level
        }
    }
    override fun getLayoutRes(): Int {
        return R.layout.fragment_home
    }

    //Try to bind widget with ID to add it to the main screen
    private fun bindWidget(){
        //Try to bind it without permission
        val res = widgetManager.bindAppWidgetIdIfAllowed(widgetHost.allocateAppWidgetId(),context?.let { ComponentName(it, WeatherWidget::class.java) })
        //If return false then ask for permission
        if(!res){
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetHost.allocateAppWidgetId())
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, context?.let { ComponentName(it, WeatherWidget::class.java) })
            }
            //Launch permission dialog with pre-defined callback
            bindCallback.launch(intent)
        }
    }

    //Show widget on the screen
    private fun showWidget(widgetID: Int) {
        val widgetInfo = widgetID.let { widgetManager.getAppWidgetInfo(it) }
        val hostView = widgetID.let { widgetHost.createView(context, it,widgetInfo) }
        hostView?.setAppWidget(widgetID,widgetInfo)
        //Create parent layout for widget
        val layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        //It should be below clock and battery
        layoutParams.addRule(RelativeLayout.BELOW,R.id.charge_tv)
        mainContainer.addView(hostView, layoutParams)
    }

    override fun onStart() {
        super.onStart()
        //Start observing time with viewModel as broadcast callback
        mViewModel.setCurrentTime()
        timeReceiver = TimeReceiver(mViewModel)
        activity?.registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        observeTime()

        //Start observing charge with viewModel as broadcast callback
        chargeReceiver = ChargeReceiver(mViewModel)
        activity?.registerReceiver(chargeReceiver,IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        observeCharge()

        for(id in widgetHost.appWidgetIds){
            //Show all widgets saved by host
            showWidget(id)

            //Update all of them
            val intent = Intent(context, WeatherWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids: IntArray = widgetManager.getAppWidgetIds(ComponentName(LauncherApplication.instance, WeatherWidget::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            activity?.sendBroadcast(intent)
        }
        //Listen for updates of all widgets
        widgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        //Stop observing time and charge
        activity?.unregisterReceiver(timeReceiver)
        activity?.unregisterReceiver(chargeReceiver)
        //Stop listening for updates of all widgets
        widgetHost.stopListening()
    }


}