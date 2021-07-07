package com.expert.qrgenerator.view.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.DialogPrefs

abstract class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun rateUs(context: AppCompatActivity) {
        val inflater = context.layoutInflater
        val view = inflater.inflate(R.layout.layout_dialog_rate_us, null)
        val builder = AlertDialog.Builder(context)
            .setCancelable(false)
            .setView(view)

        val later = view.findViewById<AppCompatTextView>(R.id.laterTv)
        val ratingBar = view.findViewById<AppCompatRatingBar>(R.id.ratingBar)

        val alertDialog = builder.show()
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (rating <= 4.0) {
                contactSupport(context)
                alertDialog.dismiss()
            } else {
                rateAppOnPlay(context)
                alertDialog.dismiss()
            }
        }
        later.setOnClickListener {
            DialogPrefs.clearPreferences(context)
            alertDialog.dismiss()
        }
    }

    private fun rateAppOnPlay(context: AppCompatActivity) {
        val rateIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + context.packageName)
            )
        context.startActivity(rateIntent)
    }

    fun contactSupport(context: AppCompatActivity) {
        val intent = Intent(Intent.ACTION_SENDTO)
        // only email apps should handle this
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.support_email)))
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No app found to handle this intent", Toast.LENGTH_LONG)
                .show()
        }


    }

}