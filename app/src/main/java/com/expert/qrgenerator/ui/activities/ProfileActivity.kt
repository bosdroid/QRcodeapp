package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityProfileBinding
import com.expert.qrgenerator.model.User
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var binding:ActivityProfileBinding
    private lateinit var context: Context
    private var user:User?=null
    private lateinit var appSettings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        displayUserDetail()

    }

    private fun initViews(){
        context = this
        appSettings = AppSettings(context)
        if (Constants.userData != null){
            user  = Constants.userData
        }

        binding.backArrow.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun displayUserDetail(){
        if (user != null){
            Glide.with(context).load(user!!.personPhoto)
                .into(binding.profileImage)
            binding.profileName.text = user!!.personName
            binding.profileEmail.text = user!!.personEmail
        }
    }
}