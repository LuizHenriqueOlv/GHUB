package com.luiz.ghub.models

import com.google.firebase.Timestamp
import java.util.Date

data class ProjectUpdate(
    val id: String = "",
    val title: String = "",
    val desc: String = "",
    val imgs: List<String> = emptyList(),
    val createdAt: Date? = null
)