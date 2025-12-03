package com.luiz.ghub.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String? = "",
    val email: String? = "",
    val bio: String? = "",
    val photoURL: String? = "",
    val bannerURL: String? = "",
    val professions: List<String>? = emptyList(),
    val skills: List<String>? = emptyList()
) : Parcelable