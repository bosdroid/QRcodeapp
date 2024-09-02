package com.expert.qrgenerator.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class representing feedback details.
 *
 * @property comment The feedback comment provided by the user.
 * @property email The email address of the user providing the feedback.
 * @property id Unique identifier for the feedback.
 * @property phone The phone number of the user providing the feedback.
 * @property qrId QR code identifier related to the feedback.
 * @property rating The rating given by the user.
 */
data class Feedback(
    @SerializedName("comment")
    val comment: String, // Feedback comment

    @SerializedName("email")
    val email: String, // User's email address

    @SerializedName("id")
    val id: Int, // Unique feedback ID

    @SerializedName("phone")
    val phone: String, // User's phone number

    @SerializedName("qrId")
    val qrId: String, // QR code ID

    @SerializedName("rating")
    val rating: String // Rating given by the user
) : Serializable
