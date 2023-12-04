package com.expert.qrgenerator.model

import java.io.Serializable

data class SNPayload (
          val sn_banner_image:String,
          val sn_content_detail_background_color:String,
          val sn_title_text:String,
          val sn_title_text_color:String,
          val sn_description_text:String,
          val sn_description_text_color:String,
          val sn_selected_social_network:ArrayList<SocialNetwork>
        ):Serializable{

        }