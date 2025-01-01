package com.expert.qrgenerator.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.graphics.Typeface
import android.view.View
import androidx.core.widget.NestedScrollView
import com.expert.qrgenerator.model.TargetData
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView

class TapTargetHelper(private val activity: Activity) {

    val appSettings = AppSettings(activity)
    // Function for showing a single target
    fun showSingleTarget(
        key: String,
        view: View,
        title: String,
        description: String,
        onTargetClick: (() -> Unit)? = null
    ) {
        if (isTargetCompleted(key)) return // Skip if already completed
        TapTargetView.showFor(activity,
            TapTarget.forView(view, title, description)
                .outerCircleColor(android.R.color.holo_blue_bright)
                .outerCircleAlpha(0.96f)
                .targetCircleColor(android.R.color.white)
                .titleTextSize(20)
                .titleTextColor(android.R.color.white)
                .descriptionTextSize(16)
                .descriptionTextColor(android.R.color.white)
                .textColor(android.R.color.white)
                .textTypeface(Typeface.SANS_SERIF)
                .dimColor(android.R.color.black)
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .transparentTarget(false)
                .targetRadius(60),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    markTargetCompleted(key) // Mark as completed
                    onTargetClick?.invoke()
                }
            })
    }

    // Function for showing a sequence of targets
    fun showSequence(
        key: String,
        nestedScrollView: NestedScrollView?,
        targets: List<TargetData>,
        onSequenceFinish: (() -> Unit)? = null,
        onSequenceCanceled: (() -> Unit)? = null,
        onSequenceStep: ((target: TargetData, targetClicked: Boolean) -> Unit)? = null
    ) {
        if (isTargetCompleted(key)) return // Skip if already completed

        // Map TargetData to TapTarget
        val targetMap = targets.associateWith { target ->
            TapTarget.forView(
                target.view,
                target.title,
                target.description
            )
                .outerCircleColor(android.R.color.holo_orange_light)
                .outerCircleAlpha(0.95f)
                .targetCircleColor(android.R.color.white)
                .titleTextSize(20)
                .titleTextColor(android.R.color.black)
                .descriptionTextSize(16)
                .descriptionTextColor(android.R.color.white)
                .textColor(android.R.color.black)
                .dimColor(android.R.color.black)
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(false)
                .transparentTarget(false)
                .targetRadius(70)
        }

        val tapTargets = targetMap.values.toList()
        val targetKeys = targetMap.keys.toList()

        var currentIndex = 0

        fun scrollToTarget(targetData: TargetData, callback: () -> Unit) {
            nestedScrollView!!.post {
                nestedScrollView.smoothScrollTo(0, targetData.view.top)
                nestedScrollView.postDelayed(callback, 300)
            }
        }

        fun showNextTarget() {
            if (currentIndex >= tapTargets.size) {
                markTargetCompleted(key) // Mark as completed
                onSequenceFinish?.invoke()
                return
            }

            val targetData = targetKeys[currentIndex]
            val tapTarget = tapTargets[currentIndex]
             if(nestedScrollView != null) {
                 scrollToTarget(targetData) {

                     TapTargetView.showFor(activity, tapTarget, object : TapTargetView.Listener() {
                         override fun onTargetClick(view: TapTargetView) {
                             super.onTargetClick(view)
                             currentIndex++
                             showNextTarget()
                         }

                         override fun onTargetCancel(view: TapTargetView) {
                             super.onTargetCancel(view)
                             onSequenceCanceled?.invoke()
                         }
                     })
                 }
             }
            else{
                 TapTargetView.showFor(activity, tapTarget, object : TapTargetView.Listener() {
                     override fun onTargetClick(view: TapTargetView) {
                         super.onTargetClick(view)
                         currentIndex++
                         showNextTarget()
                     }

                     override fun onTargetCancel(view: TapTargetView) {
                         super.onTargetCancel(view)
                         onSequenceCanceled?.invoke()
                     }
                 })
             }
        }

        showNextTarget()
    }


    // Check if the target has been completed
    private fun isTargetCompleted(key: String): Boolean {
        return appSettings.getBoolean(key)
    }

    // Mark the target as completed
    private fun markTargetCompleted(key: String) {
        appSettings.putBoolean(key, true)
    }
}
