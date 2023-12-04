package com.expert.qrgenerator.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Feedback(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("qrId")
    val qrId: String,
    @SerializedName("rating")
    val rating: String
):Serializable{

}