package com.expert.qrgenerator.model

import java.io.Serializable

data class SocialNetwork (
        var icon:Int,
        var title:String,
        var url:String,
        var isActive:Int
        ):Serializable{

}