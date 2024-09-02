package com.expert.qrgenerator.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class representing a coupon with various attributes.
 * @property couponCompanyName The name of the company offering the coupon.
 * @property couponBackgroundColor The background color of the coupon.
 * @property couponHeaderImage The URL or path to the coupon's header image.
 * @property couponSaleBadgeButtonText The text displayed on the sale badge button.
 * @property couponSaleBadgeButtonColor The color of the sale badge button.
 * @property couponHeadlineText The headline text for the coupon.
 * @property couponDescriptionText The description text for the coupon.
 * @property couponGetButtonText The text displayed on the 'Get' button.
 * @property couponGetButtonColor The color of the 'Get' button.
 * @property couponCodeText The coupon code text.
 * @property couponCodeTextColor The color of the coupon code text.
 * @property couponValidDate The date until which the coupon is valid.
 * @property couponTermsConditionText The terms and conditions of the coupon.
 * @property couponRedeemButtonText The text displayed on the 'Redeem' button.
 * @property couponRedeemButtonColor The color of the 'Redeem' button.
 * @property couponRedeemWebsiteUrl The URL where the coupon can be redeemed.
 */
data class Coupon(
    @SerializedName("coupon_company_name")
    val couponCompanyName: String = "",

    @SerializedName("coupon_background_color")
    val couponBackgroundColor: String = "",

    @SerializedName("coupon_header_image")
    val couponHeaderImage: String = "",

    @SerializedName("coupon_sale_badge_button_text")
    val couponSaleBadgeButtonText: String = "",

    @SerializedName("coupon_sale_badge_button_color")
    val couponSaleBadgeButtonColor: String = "",

    @SerializedName("coupon_headline_text")
    val couponHeadlineText: String = "",

    @SerializedName("coupon_description_text")
    val couponDescriptionText: String = "",

    @SerializedName("coupon_get_button_text")
    val couponGetButtonText: String = "",

    @SerializedName("coupon_get_button_color")
    val couponGetButtonColor: String = "",

    @SerializedName("coupon_code_text")
    val couponCodeText: String = "",

    @SerializedName("coupon_code_text_color")
    val couponCodeTextColor: String = "",

    @SerializedName("coupon_valid_date")
    val couponValidDate: String = "",

    @SerializedName("coupon_terms_condition_text")
    val couponTermsConditionText: String = "",

    @SerializedName("coupon_redeem_button_text")
    val couponRedeemButtonText: String = "",

    @SerializedName("coupon_redeem_button_color")
    val couponRedeemButtonColor: String = "",

    @SerializedName("coupon_redeem_website_url")
    val couponRedeemWebsiteUrl: String = ""
) : Serializable
