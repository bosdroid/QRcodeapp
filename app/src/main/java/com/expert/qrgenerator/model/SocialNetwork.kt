package com.expert.qrgenerator.model

import java.io.Serializable

/**
 * Data class representing a social network entity.
 *
 * @property iconName The name of the icon associated with the social network.
 * @property icon The resource ID of the icon.
 * @property title The title of the social network.
 * @property description A brief description of the social network.
 * @property url The URL associated with the social network.
 * @property isActive Indicator of whether the social network is active (1 for active, 0 for inactive).
 */
data class SocialNetwork(
    var iconName: String = "",
    var icon: Int = 0,
    var title: String = "",
    var description: String = "",
    var url: String = "",
    var isActive: Int = 0
) : Serializable {

    /**
     * Secondary constructor for creating a SocialNetwork instance with default properties values.
     */
    constructor() : this("",0,"","","",0)
}
