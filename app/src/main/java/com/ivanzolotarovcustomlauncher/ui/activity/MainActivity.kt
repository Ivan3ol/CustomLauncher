package com.ivanzolotarovcustomlauncher.ui.activity

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.ivanzolotarovcustomlauncher.R
import com.ivanzolotarovcustomlauncher.base.BaseActivity
import com.ivanzolotarovcustomlauncher.databinding.ActivityMainBinding
import com.ivanzolotarovcustomlauncher.ui.appdrawer.AppDrawerFragment
import com.ivanzolotarovcustomlauncher.ui.home.HomeFragment


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mViewModel = MainViewModel()
        mBinding.viewModel = mViewModel

        //By default go to Home Fragment
        showHomeFragment()
    }

    override fun onStart() {
        super.onStart()
        //Always hide status bar as it is launcher app
        hideSystemUI()
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    //Go to App Drawer Fragment
    fun showAppFragment(){
        showFragment(AppDrawerFragment(),"Drawer")
    }
    //Go to Home Fragment
    fun showHomeFragment(){
        showFragment(HomeFragment(),"Home")
    }

    //Go to any fragment
    private fun showFragment(fragment: Fragment, tag: String) {
        val manager: FragmentManager = supportFragmentManager
        manager.beginTransaction()
            .replace(R.id.container, fragment, tag)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    //Track Android back button actions
    override fun onBackPressed() {
        super.onBackPressed()
        //If App Drawer fragment is open then go to Home Fragment, otherwise do nothing
        val currentFragment = supportFragmentManager.findFragmentByTag("Drawer")
        if (currentFragment != null && currentFragment.isVisible) {
            showHomeFragment()
        }
    }

    //Method that hides status bar
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

}

