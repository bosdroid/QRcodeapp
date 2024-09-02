package com.expert.qrgenerator.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(private val mContext: Context) {

    // Lazy initialization of RequestQueue
    private val mRequestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(mContext.applicationContext)
    }

    /**
     * Provides the request queue instance.
     */
    val requestQueue: RequestQueue
        get() = mRequestQueue

    /**
     * Adds a request to the RequestQueue.
     * @param request The request to be added.
     */
    fun <T> addToRequestQueue(request: Request<T>) {
        requestQueue.add(request)
    }

    companion object {
        // Singleton instance of VolleySingleton
        @Volatile
        private var mInstance: VolleySingleton? = null

        /**
         * Provides the singleton instance of VolleySingleton.
         * @param context The context of the application.
         * @return The singleton instance of VolleySingleton.
         */
        @JvmStatic
        fun getInstance(context: Context): VolleySingleton {
            return mInstance ?: synchronized(this) {
                mInstance ?: VolleySingleton(context).also { mInstance = it }
            }
        }
    }
}
