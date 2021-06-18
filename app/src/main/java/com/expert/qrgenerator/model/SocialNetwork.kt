package com.expert.qrgenerator.model

import java.io.Serializable

data class SocialNetwork(
        var iconName: String,
        var icon: Int,
        var title: String,
        var description:String,
        var url: String,
        var isActive: Int
) : Serializable {

    constructor(iconName: String, title: String, url: String) : this("", 0, "","", "", 0) {
        this.iconName = iconName
        this.title = title
        this.url = url
    }

}