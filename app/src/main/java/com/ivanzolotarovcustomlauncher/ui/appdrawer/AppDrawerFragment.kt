package com.ivanzolotarovcustomlauncher.ui.appdrawer

import android.os.Bundle
import android.view.View
import com.ivanzolotarovcustomlauncher.R
import com.ivanzolotarovcustomlauncher.app.LauncherApplication
import com.ivanzolotarovcustomlauncher.base.BaseFragment
import com.ivanzolotarovcustomlauncher.databinding.FragmentAppDrawerBinding

//Fragment with apps recycler view
class AppDrawerFragment : BaseFragment<FragmentAppDrawerBinding, AppDrawerViewModel>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = AppDrawerViewModel()
        mBinding.viewModel = mViewModel
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_app_drawer
    }

}