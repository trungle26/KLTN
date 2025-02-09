package com.trungld.viberide.data.entities

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Media(
    val artist: String = "",
    @PropertyName("file_url") val file_url: String = "",
    val genre: String = "",
    @PropertyName("thumbnail_url") val thumbnail_url: String = "",
    val title: String = "",
    val type: String = ""
) : Parcelable