package com.expert.qrgenerator.model

import java.io.Serializable

/**
 * Data class representing a User.
 * Implements Serializable for object serialization.
 *
 * @property personName Full name of the person.
 * @property personGivenName Given name of the person.
 * @property personFamilyName Family name of the person.
 * @property personEmail Email address of the person.
 * @property personId Unique identifier for the person.
 * @property personPhoto URL or path to the person's photo.
 * @property id Optional integer ID, default value is null.
 */
data class User(
    val personName: String,
    val personGivenName: String,
    val personFamilyName: String,
    val personEmail: String,
    val personId: String,
    val personPhoto: String
) : Serializable {

    // Optional integer ID for the user, default value is null
    val id: Int? = null

    /**
     * Returns a string representation of the User object.
     *
     * @return A string describing the User object.
     */
    override fun toString(): String {
        return "User(personName='$personName', personGivenName='$personGivenName', personFamilyName='$personFamilyName', personEmail='$personEmail', personId='$personId', personPhoto='$personPhoto')"
    }
}
