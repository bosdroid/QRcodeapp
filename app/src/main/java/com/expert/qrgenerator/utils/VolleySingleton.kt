package com.expert.qrgenerator.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton (var mContext: Context) {
    private var mRequestQueue: RequestQueue?
    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(mContext)
            }
            return mRequestQueue
        }

    fun <T> addToRequestQueue(request: Request<T>?) {
        requestQueue!!.add(request)
    }

    companion object {
        private var mInstance: VolleySingleton? = null
        @Synchronized
        fun getInstance(context: Context): VolleySingleton? {
            if (mInstance == null) {
                mInstance = VolleySingleton(context)
            }
            return mInstance
        }
    }

    init {
        mRequestQueue = requestQueue
    }
}