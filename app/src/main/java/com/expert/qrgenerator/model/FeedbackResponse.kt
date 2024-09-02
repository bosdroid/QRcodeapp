package com.expert.qrgenerator.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the response containing a list of feedbacks.
 *
 * @property feedbacks A list of feedback objects.
 */
data class FeedbackResponse(
    @SerializedName("feedbacks")
    val feedbacks: List<Feedback> // Use List instead of ArrayList for immutability
)
