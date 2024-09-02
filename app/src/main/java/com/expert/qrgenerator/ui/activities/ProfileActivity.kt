package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.os.Bundle
import com.bumptech.glide.Glide
import com.expert.qrgenerator.databinding.ActivityProfileBinding
import com.expert.qrgenerator.model.User
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }
    private var user: User? = null
    private lateinit var appSettings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and settings
        initViews()
        // Display user details
        displayUserDetail()
    }

    private fun initViews() {

        appSettings = AppSettings(context)

        // Retrieve user data from Constants if available
        user = Constants.userData

        // Set up click listener for back button
        binding.backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun displayUserDetail() {
        // Display user details if user is not null
        user?.let {
            // Load user photo using Glide
            Glide.with(context)
                .load(it.personPhoto)
                .into(binding.profileImage)

            // Set user details to UI components
            binding.profileName.text = it.personName
            binding.profileEmail.text = it.personEmail
        }
    }
}
