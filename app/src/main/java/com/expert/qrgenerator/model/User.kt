package com.expert.qrgenerator.model

import android.net.Uri
import java.io.Serializable

data class User (
           val personName:String,
           val personGivenName:String,
           val personFamilyName:String,
           val personEmail:String,
           val personId:String,
           val personPhoto:String
        ):Serializable{

    val id:Int?=null

    override fun toString(): String {
        return "User(personName='$personName', personGivenName='$personGivenName', personFamilyName='$personFamilyName', personEmail='$personEmail', personId='$personId', personPhoto=$personPhoto)"
    }
}