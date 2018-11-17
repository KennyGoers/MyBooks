package com.trixiesoft.mybooks

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import com.trixiesoft.mybooks.db.AppDatabase
import com.trixiesoft.mybooks.db.Book
import com.trixiesoft.mybooks.db.getCoverUrlMedium
import com.trixiesoft.mybooks.utils.bindView
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber


class MainActivity : AppCompatActivity() {
    val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    val addButton: FloatingActionButton by bindView(R.id.add_book)
    val progress: ProgressBar by bindView(R.id.progress)

    companion object {
        const val REQ_ADD_BOOK = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addButton.setOnClickListener {
            startActivityForResult(Intent(this, BookList::class.java), REQ_ADD_BOOK)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BookAdapter(mutableListOf())
    }

    override fun onStart() {
        super.onStart()

        val dao = AppDatabase.getDatabase(this)?.dao ?: return
        getDisposable = dao.getAllBooksFlowable()
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribeWith(object : DisposableSubscriber<List<Book>>() {
                override fun onNext(books: List<Book>) {
                    Log.d("MainActivity", "onNext")
                    updateBooks(books)
                }
                override fun onError(t: Throwable) {
                    Log.e("MainActivity", "onError", t)
                }
                override fun onComplete() {
                    Log.d("MainActivity", "onComplete")
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQ_ADD_BOOK) {
            val book = data?.getParcelableExtra<com.trixiesoft.mybooks.api.Book>("book")
            if (book != null)
                addBook(book)
        }
    }

    fun error(error: Throwable) {
    }

    fun busy(busy: Boolean) {
        progress.visibility = if (busy) View.VISIBLE else View.GONE
    }

    fun markAsRead(book: Book) {
        Completable
            .fromCallable { AppDatabase.getDatabase(this)?.dao?.setBookReadFlag(book.id, !book.read) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(@NonNull d: Disposable) {
                    insertDisposable = d
                }
                override fun onComplete() {
                    insertDisposable = null
                }
                override fun onError(@NonNull e: Throwable) {
                    insertDisposable = null
                }
            })
    }

    private fun addBook(book: com.trixiesoft.mybooks.api.Book) {
        // don't bother with a busy display, too short to notice
        Completable.fromCallable {
                AppDatabase.getDatabase(this)?.dao?.
                    insertBooks(Book(
                        book.key,
                        book.coverI,
                        if (book.isbn != null) book.isbn[0] else "",
                        book.title,
                        book.authorName[0],
                        false
                    ))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(@NonNull d: Disposable) {
                    insertDisposable = d
                }
                override fun onComplete() {
                    insertDisposable = null
                }
                override fun onError(@NonNull e: Throwable) {
                    insertDisposable = null
                }
            })
    }

    fun updateBooks(books: List<Book>) {
        (recyclerView.adapter as BookAdapter).updateList(books)
    }

    override fun onStop() {
        super.onStop()
        getDisposable?.dispose()
    }

    private var getDisposable: Disposable? = null
    private var insertDisposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        insertDisposable?.dispose()
    }

    class BookDiff(val newList: List<Book>, val oldList: List<Book>): DiffUtil.Callback() {
        override fun areItemsTheSame(old: Int, new: Int): Boolean  = oldList[old] == newList[new]
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areContentsTheSame(old: Int, new: Int) = oldList[old] == newList[new]
    }

    inner class BookAdapter(var bookList: List<Book>) : RecyclerView.Adapter<BookViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            return BookViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_book_main, parent, false))
        }

        override fun getItemCount(): Int {
            return bookList.size
        }

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            holder.bind(bookList[position])
        }

        fun updateList(books: List<Book>) {
            val result = DiffUtil.calculateDiff(BookDiff(books, bookList))
            bookList = books
            result.dispatchUpdatesTo(this)
        }
    }

    fun bookLongPress(book: Book) {
        AlertDialog.Builder(this)
            .setTitle("Mark As Read")
            .setIcon(if (book.read) R.drawable.ic_book_read else R.drawable.ic_book)
            .setMessage("Marker the current book as having been read?")
            .setPositiveButton("Yes", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    markAsRead(book)
                }
            })
            .setCancelable(true)
            .show()
    }

    inner class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView by bindView(R.id.list_item_header)

        init {
        }

        fun bind() {
        }
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var book: Book? = null
        val title: TextView by bindView(R.id.txtTitle)
        val author: TextView by bindView(R.id.txtAuthor)
        val image: ImageView by bindView(R.id.imgThumbnail)
        val target: com.squareup.picasso.Target

        init {
            itemView.setOnClickListener { /*future use*/ }
            itemView.setOnLongClickListener { bookLongPress(book!!); true }
            target = object: com.squareup.picasso.Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable) {
                    image.scaleType = ImageView.ScaleType.CENTER
                    image.setImageDrawable(placeHolderDrawable)
                }
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    image.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    image.setImageBitmap(bitmap)
                }
                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                    image.scaleType = ImageView.ScaleType.CENTER
                    image.setImageResource(if (book != null && book!!.read) R.drawable.ic_book_read else R.drawable.ic_book)
                }
            }
        }

        fun bind(book: Book) {
            this.book = book
            title.text = book.title
            author.text = book.author
            val url = book.getCoverUrlMedium()
            if (url != null) {
                Picasso.get()
                    .load(url)
                    .placeholder(if (book.read) R.drawable.ic_book_read else R.drawable.ic_book)
                    .into(target)
            } else {
                image.scaleType = ImageView.ScaleType.CENTER
                image.setImageResource(if (book.read) R.drawable.ic_book_read else R.drawable.ic_book)
            }
        }
    }
}
