package com.trixiesoft.mybooks.ui

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.trixiesoft.mybooks.R
import com.trixiesoft.mybooks.ui.widgets.TouchyRecyclerView
import com.trixiesoft.mybooks.api.*
import com.trixiesoft.mybooks.utils.bindView
import com.trixiesoft.mybooks.utils.getParent
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * A placeholder fragment containing a simple view.
 */
class FindBookFragment : Fragment() {

    interface FindBookInterface {
        fun bookFound(book: com.trixiesoft.mybooks.db.Book)
        fun cancelFind()
    }

    fun View.hideSoftKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_books, container, false)
    }

    private val recyclerView: TouchyRecyclerView by bindView(R.id.recyclerView)
    private val navigationButton: View by bindView(R.id.navigationButton)
    private val clearButton: View by bindView(R.id.clearButton)
    private val searchEditText: EditText by bindView(R.id.searchEditText)
    private val searchEditOverlay: View by bindView(R.id.searchEditOverlay)
    private val busy: ProgressBar by bindView(R.id.busy)


    var editTextDisposable: Disposable? = null

    @SuppressLint("RxDefaultScheduler")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.monitorTouch = true
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = BookAdapter(mutableListOf())
        navigationButton.setOnClickListener {
            (getParent() as FindBookInterface).cancelFind()
            //if (getParent() is FindBookInterface)
            //    (getParent() as FindBookInterface).cancelFind()
            //activity?.finish()
        }
        clearButton.setOnClickListener {
            searchEditText.setText("")
            recyclerView.adapter = BookAdapter(mutableListOf())
        }

        // set up a debounce on text input such that we minimize the queries while typing
        editTextDisposable = Observable.create(ObservableOnSubscribe<String> { subscriber ->
            searchEditText.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    val searchText = p0?.toString()
                    if (searchText.isNullOrEmpty()) {
                        clearButton.visibility = View.GONE
                    } else {
                        clearButton.visibility = View.VISIBLE
                    }
                    subscriber.onNext(searchText!!)
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }})
            })
            .map { text -> text.toLowerCase().trim() }
            .debounce(500, TimeUnit.MILLISECONDS)
            .distinct()
            //.filter { text -> text.isNotBlank() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({text -> onSearch(text)}, {error -> showError(error)} )

        // if the user starts interacting with the list, hide the keyboard
        recyclerView.onTouched = object: TouchyRecyclerView.OnTouched {
            override fun onTouched() {
                // close the keyboard
                searchEditText.hideSoftKeyboard()
                // kill focus from edit
                searchEditOverlay.requestFocus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editTextDisposable?.dispose()
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
        disposable?.dispose()
        disposable = null
        if (searchText.length <= 1) {
            Log.i("onSearch", "Cancel Search")
            busy.visibility = View.GONE
            recyclerView.adapter = BookAdapter(mutableListOf())
            return
        }
        Log.i("onSearch", "Search -> $searchText")
        busy.visibility = View.VISIBLE
        disposable = Single.zip(
            BookAPI.instance.searchBooksByTitle(searchText),
            BookAPI.instance.searchBooksByAuthor(searchText),
            BiFunction { a: BookResult, b: BookResult -> bundle(a, b) }
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    busy.visibility = View.GONE
                    updateList(result)
                    disposable = null
                },
                { error ->
                    busy.visibility = View.GONE
                    showError(error)
                    disposable = null
                }
            )
    }

    private fun selectBook(book: Book) {
        val bookData = com.trixiesoft.mybooks.db.Book(
            book.key,
            book.coverI,
            if (book.isbn != null) book.isbn[0] else "",
            book.title,
            book.authorName[0],
            false
        )
        (getParent() as FindBookInterface).bookFound(bookData)
        //activity?.setResult(RESULT_OK, Intent().putExtra("book", book))
        //activity?.finish()
    }

    private fun showError(error: Throwable) {
        // Yes, it is a NOOP for now :/
    }

    private fun updateList(books: List<Book>) {
        (recyclerView.adapter as BookAdapter).updateList(books)
    }

    class BookDiff(private val newList: List<Book>, private val oldList: List<Book>): DiffUtil.Callback() {
        override fun areItemsTheSame(old: Int, new: Int): Boolean  = oldList[old] == newList[new]
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areContentsTheSame(old: Int, new: Int) = oldList[old] == newList[new]
    }

    inner class BookAdapter(private var bookList: List<Book>) : RecyclerView.Adapter<BookViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            return BookViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_book, parent, false))
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
        val image: ImageView by bindView(R.id.imgThumbnail)
        val target: com.squareup.picasso.Target

        init {
            itemView.setOnClickListener {
                this@FindBookFragment.selectBook(book!!)
            }
            target = object: com.squareup.picasso.Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    image.scaleType = ImageView.ScaleType.CENTER
                    image.setImageDrawable(placeHolderDrawable)
                }
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    image.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    image.setImageBitmap(bitmap)
                }
                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                    image.scaleType = ImageView.ScaleType.CENTER
                    image.setImageResource(R.drawable.ic_book)
                }
            }
        }

        fun bind(book: Book) {
            this.book = book
            title.text = book.title
            if (book.authorName.isNullOrEmpty())
                author.text = getString(R.string.unknown)
            else
                author.text = book.authorName.first()
            val url = book.getCoverUrlMedium()
            if (url != null)
                Picasso.get()
                    .load(url)
                    .error(R.drawable.ic_book)
                    .placeholder(R.drawable.ic_book)
                    .into(target)
            else
                image.setImageResource(R.drawable.ic_book)
        }
    }
}

