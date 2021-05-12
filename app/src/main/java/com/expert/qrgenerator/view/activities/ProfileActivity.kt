package com.expert.qrgenerator.view.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.User
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.google.android.material.textview.MaterialTextView
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : BaseActivity() {

    private lateinit var context: Context
    private var user:User?=null
    private lateinit var appSettings: AppSettings
    private lateinit var profileImageView:CircleImageView
    private lateinit var profileNameView:MaterialTextView
    private lateinit var profileEmailView:MaterialTextView
    private lateinit var backArrow:AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        displayUserDetail()

    }

    private fun initViews(){
        context = this
        appSettings = AppSettings(context)
        if (Constants.userData != null){
            user  = Constants.userData
        }
        profileImageView = findViewById(R.id.profile_image)
        profileNameView = findViewById(R.id.profile_name)
        profileEmailView = findViewById(R.id.profile_email)
        backArrow = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun displayUserDetail(){
        if (user != null){
            Glide.with(context).load(user!!.personPhoto)
                .into(profileImageView)
            profileNameView.text = user!!.personName
            profileEmailView.text = user!!.personEmail
        }
    }
}