package com.trixiesoft.mybooks.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


fun Book.getCoverUrlSmall(): String? {
    if (coverIndex != null) {
        return String.format("http://covers.openlibrary.org/b/id/%s-S.jpg?default=false", coverIndex)
    }
    return null
}

fun Book.getCoverUrlMedium(): String? {
    if (coverIndex != null) {
        return String.format("http://covers.openlibrary.org/b/id/%s-M.jpg?default=false", coverIndex)
    } else if (isbn != null) {
        return String.format("http://covers.openlibrary.org/b/isbn/%s-M.jpg?default=false", isbn)
    }
    return null
}

fun Book.getCoverUrlLarge(): String? {
    if (coverIndex != null) {
        return String.format("http://covers.openlibrary.org/b/id/%s-L.jpg?default=false", coverIndex)
    } else if (isbn != null) {
        return String.format("http://covers.openlibrary.org/b/isbn/%s-L.jpg?default=false", isbn)
    }
    return null
}

@Entity(tableName = "book", indices = [Index(value = ["title"]), Index(value=["author"])])
data class Book(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "cover_index") val coverIndex: String?,
    @ColumnInfo(name = "isbn") val isbn: String?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "read") val read: Boolean
)
