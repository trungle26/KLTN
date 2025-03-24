package com.trungld.viberide.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.PropertyName
import com.trungld.viberide.data.GenreConverter
import kotlinx.parcelize.Parcelize

@Entity(tableName = "media")
@TypeConverters(GenreConverter::class)
@Parcelize
data class Media(
    @PrimaryKey val id: String = "",
    val artist: String = "",
    @PropertyName("file_url") val file_url: String = "",
    @TypeConverters(GenreConverter::class) val genre: List<String> = emptyList<String>(),
    @PropertyName("thumbnail_url") val thumbnail_url: String = "",
    val title: String = "",
    val type: String = "" // video or music
) : Parcelable
