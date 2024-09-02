package com.expert.qrgenerator.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Represents the payload for social network data.
 *
 * @property bannerImage URL or path to the banner image.
 * @property contentDetailBackgroundColor Hex code or color name for the background color of the content detail.
 * @property titleText The title text displayed on the social network payload.
 * @property titleTextColor Hex code or color name for the title text color.
 * @property descriptionText The description text for the social network payload.
 * @property descriptionTextColor Hex code or color name for the description text color.
 * @property selectedSocialNetwork List of selected social networks associated with this payload.
 */
data class SNPayload(
    @SerializedName("sn_banner_image")
    val bannerImage: String,  // URL or path to the banner image

    @SerializedName("sn_content_detail_background_color")
    val contentDetailBackgroundColor: String,  // Background color for content detail (e.g., hex code)

    @SerializedName("sn_title_text")
    val titleText: String,  // Title text

    @SerializedName("sn_title_text_color")
    val titleTextColor: String,  // Color for the title text (e.g., hex code)

    @SerializedName("sn_description_text")
    val descriptionText: String,  // Description text

    @SerializedName("sn_description_text_color")
    val descriptionTextColor: String,  // Color for the description text (e.g., hex code)

    @SerializedName("sn_selected_social_network")
    val selectedSocialNetwork: ArrayList<SocialNetwork>  // List of selected social networks
) : Serializable
