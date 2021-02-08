package com.expert.qrgenerator.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(model: ViewModel?) : ViewModelProvider.Factory{

    private var model: ViewModel? = null

    init {
        this.model = model
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(model!!.javaClass)) {
            return model as T
        }
        throw IllegalArgumentException("unexpected model class $modelClass")
    }

    fun <T : ViewModel?> createFor(): ViewModelProvider.Factory? {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(model!!.javaClass)) {
                    return model as T
                }
                throw IllegalArgumentException("unexpected model class $modelClass")
            }
        }
    }
}