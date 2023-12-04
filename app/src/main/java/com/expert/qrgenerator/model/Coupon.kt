package com.expert.qrgenerator.model

import java.io.Serializable

data class Coupon (
    var coupon_company_name:String = "",
    var coupon_background_color:String = "",
    var coupon_header_image:String = "",
    var coupon_sale_badge_button_text:String = "",
    var coupon_sale_badge_button_color:String = "",
    var coupon_headline_text:String = "",
    var coupon_description_text:String = "",
    var coupon_get_button_text:String = "",
    var coupon_get_button_color:String = "",
    var coupon_code_text:String = "",
    var coupon_code_text_color:String = "",
    var coupon_valid_date:String = "",
    var coupon_terms_condition_text:String = "",
    var coupon_redeem_button_text:String = "",
    var coupon_redeem_button_color:String = "",
    var coupon_redeem_website_url:String = ""
        ) : Serializable{

        }