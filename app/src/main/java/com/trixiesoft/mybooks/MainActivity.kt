package com.trixiesoft.mybooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.trixiesoft.mybooks.db.AppDatabase
import com.trixiesoft.mybooks.db.Book
import com.trixiesoft.mybooks.utils.bindView
import io.reactivex.disposables.Disposable
import io.reactivex.subscribers.DisposableSubscriber

class MainActivity : AppCompatActivity() {
    val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    val addButton: FloatingActionButton by bindView(R.id.add_book)

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
        disposable = dao.getAllBooksFlowable()
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribeWith(object : DisposableSubscriber<List<Book>>() {
                override fun onNext(books: List<Book>) {
                    //busy(false)
                    Log.d("MainActivity", "getLiteVehicleList: onNext")
                    updateBooks(books)
                }
                override fun onError(t: Throwable) {
                    //busy(false)
                    Log.e("MainActivity", "getLiteVehicleList: onError", t)
                }
                override fun onComplete() {
                    //busy(false)
                    Log.d("MainActivity", "getLiteVehicleList: onComplete")
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQ_ADD_BOOK) {
            val book = data?.getParcelableExtra<com.trixiesoft.mybooks.api.Book>("book")

        }
    }

    fun updateBooks(books: List<Book>) {
        (recyclerView.adapter as BookAdapter).updateList(books)
    }

    override fun onStop() {
        super.onStop()
        disposable?.dispose()
    }

    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    class BookDiff(val newList: List<Book>, val oldList: List<Book>): DiffUtil.Callback() {
        override fun areItemsTheSame(old: Int, new: Int): Boolean  = oldList[old] == newList[new]
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areContentsTheSame(old: Int, new: Int) = oldList[old] == newList[new]
    }

    inner class BookAdapter(var bookList: List<Book>) : RecyclerView.Adapter<BookViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            return BookViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_view_book, parent, false))
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

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var book: Book? = null
        val title: TextView by bindView(R.id.txtTitle)
        val author: TextView by bindView(R.id.txtAuthor)

        fun bind(book: Book) {
            this.book = book
            title.text = book.title
            author.text = book.author
        }
    }
}
