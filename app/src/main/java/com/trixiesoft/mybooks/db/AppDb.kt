package com.trixiesoft.mybooks.db

import android.content.Context
import androidx.room.*
import io.reactivex.Flowable

@Database(
    entities = [ Book::class ],
    version = 3,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    @Dao
    abstract class AppDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        abstract fun insertBooks(vararg books: Book)

        @Update(onConflict = OnConflictStrategy.REPLACE)
        abstract fun updateBooks(vararg books: Book)

        @Delete
        abstract fun deleteBooks(vararg books: Book)

        @Query("SELECT * FROM book")
        abstract fun getBooks(): List<Book>

        @Query("SELECT * FROM book ORDER BY read ASC, title ASC")
        abstract fun getAllBooksFlowable(): Flowable<List<Book>>

        @Query("UPDATE book SET read = :isRead WHERE id = :id")
        abstract fun setBookReadFlag(id: String, isRead: Boolean)
    }

    abstract val dao: AppDao

    companion object {
        fun getDatabase(context: Context): AppDatabase? {
            if (database == null) {
                database = Room.databaseBuilder(context, AppDatabase::class.java, "book_db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return database
        }

        private var database: AppDatabase? = null
    }
}
