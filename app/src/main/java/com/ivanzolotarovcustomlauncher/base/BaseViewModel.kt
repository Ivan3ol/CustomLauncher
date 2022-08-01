package com.ivanzolotarovcustomlauncher.base

import androidx.annotation.NonNull
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel


open class BaseViewModel : ViewModel(), Observable {
    @Transient
    private var mCallbacks: PropertyChangeRegistry = PropertyChangeRegistry()
    //Two methods required to be overridden
    override fun addOnPropertyChangedCallback(@NonNull callback: Observable.OnPropertyChangedCallback) {
        mCallbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(@NonNull callback: Observable.OnPropertyChangedCallback) {
        mCallbacks.remove(callback)
    }
}