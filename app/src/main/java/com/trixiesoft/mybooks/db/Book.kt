package com.trixiesoft.mybooks.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "book")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "isbn") val isbn: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "read") val read: Boolean
)
/*
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        val otherBook = other as Book
        return isbn == otherBook.isbn &&
               title == otherBook.title &&
               author == otherBook.author &&
               read == otherBook.read
    }

    override fun hashCode(): Int{
        return Arrays.hashCode(board)
    }
}
*/
