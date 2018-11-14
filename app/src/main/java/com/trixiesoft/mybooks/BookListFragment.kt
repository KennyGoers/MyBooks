package com.trixiesoft.mybooks

import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trixiesoft.mybooks.api.Book
import com.trixiesoft.mybooks.api.BookAPI
import com.trixiesoft.mybooks.api.BookResult
import com.trixiesoft.mybooks.utils.bindView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

/**
 * A placeholder fragment containing a simple view.
 */
class BookListFragment : Fragment() {

    fun View.hideSoftKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    private val recyclerView: TouchyRecyclerView by bindView(R.id.recyclerView)
    private val navigationButton: View by bindView(R.id.navigationButton)
    private val clearButton: View by bindView(R.id.clearButton)
    private val searchEditText: EditText by bindView(R.id.searchEditText)
    private val searchEditOverlay: View by bindView(R.id.searchEditOverlay)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.monitorTouch = true
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = BookAdapter(mutableListOf())
        navigationButton.setOnClickListener {
            activity?.finish()
        }
        clearButton.setOnClickListener {
            searchEditText.setText("")
            recyclerView.adapter = BookAdapter(mutableListOf())
        }
        searchEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0?.toString()
                if (searchText.isNullOrEmpty()) {
                    clearButton.visibility = View.GONE
                } else {
                    clearButton.visibility = View.VISIBLE
                    onSearch(searchText)
                }
            }
        })
        recyclerView.onTouched = object: TouchyRecyclerView.OnTouched {
            override fun onTouched() {
                // close the keyboard
                searchEditText.hideSoftKeyboard()
                // kill focus from edit
                searchEditOverlay.requestFocus()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private var disposable: Disposable? = null

    private fun bundle(r1: BookResult, r2: BookResult): MutableList<Book> {
        val books: MutableList<Book> = mutableListOf()
        books.addAll(r1.books)
        books.addAll(r2.books)
        return books
    }

    private fun onSearch(searchText: String) {
        if (searchText.length <= 1) {
            recyclerView.adapter = BookAdapter(mutableListOf())
            return
        }
        disposable?.dispose()
        disposable = Single.zip(
            BookAPI.instance.searchBooksByTitle(searchText),
            BookAPI.instance.searchBooksByAuthor(searchText),
            BiFunction { a: BookResult, b: BookResult -> bundle(a, b) }
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> updateList(result); disposable = null },
                { error -> showError(error); disposable = null }
            )
    }

    fun showError(error: Throwable) {
    }

    fun updateList(books: List<Book>) {
        (recyclerView.adapter as BookAdapter).updateList(books)
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
            val book: Book = bookList[position]
            holder.bind(book)
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

        init {
            itemView.setOnClickListener {
            }
        }

        fun bind(book: Book) {
            this.book = book
            title.text = book.title
            if (book.authorName.isNullOrEmpty())
                author.text = "Unknown"
            else
                author.text = book.authorName.first()
        }
    }
}

