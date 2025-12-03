package com.luiz.ghub.models

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Project(
    @get:Exclude var id: String = "",
    val title: String = "",
    val desc: String = "",
    val state: String = "",
    val cover: String = "",
    val gens: List<String> = emptyList(),
    val goal: Double = 0.0,
    val founds: Double = 0.0,
    val ownerUid: String = ""
) : Parcelable