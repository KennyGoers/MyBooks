package com.trixiesoft.mybooks.db

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Flowable

class Data {
    companion object {

        fun getBooks(context: Context, unread: Boolean): Flowable<List<Book>> {
            val dao = AppDatabase.getDatabase(context)?.dao!!
            return if (unread) dao.getUnreadBooksFlowable() else dao.getReadBooksFlowable()
        }

        fun addBook(context: Context, book: Book): Completable {
            return Completable.fromCallable {
                AppDatabase.getDatabase(context)?.dao?.insertBooks(book)
            }
        }

        fun updateBookRead(context: Context, book: Book, bookRead: Boolean): Completable {
            return Completable.fromCallable {
                AppDatabase.getDatabase(context)?.dao?.updateBooks(book.copy(read = bookRead))
            }
        }
        fun deleteBook(context: Context, book: Book): Completable {
            return Completable.fromCallable {
                AppDatabase.getDatabase(context)?.dao?.deleteBooks(book)
            }
        }
    }
}