package com.trungld.viberide.data

import androidx.room.TypeConverter

class GenreConverter {
    @TypeConverter
    fun fromGenreList(genres: List<String>?): String? {
        return genres?.joinToString(",") // e.g., ["Relax", "Ambient"] -> "Relax,Ambient"
    }

    @TypeConverter
    fun toGenreList(genresString: String?): List<String>? {
        return genresString?.split(",")?.map { it.trim() } // e.g., "Relax,Ambient" -> ["Relax", "Ambient"]
    }
}