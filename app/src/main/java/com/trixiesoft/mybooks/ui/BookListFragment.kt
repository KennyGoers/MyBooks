package com.trixiesoft.mybooks.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.trixiesoft.mybooks.R
import com.trixiesoft.mybooks.db.Book
import com.trixiesoft.mybooks.db.Data
import com.trixiesoft.mybooks.db.getCoverUrlMedium
import com.trixiesoft.mybooks.utils.bindView
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber

class BookListFragment : Fragment() {

    val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    val emptyView: View by bindView(R.id.emptyView)

    companion object {
        fun newInstance(read: Boolean): Fragment {
            val fragment = BookListFragment()
            val arguments = Bundle()
            arguments.putBoolean("read", read)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var viewModel: BookListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    var getDisposable: Disposable? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BookListViewModel::class.java)

        val unread = !arguments!!.getBoolean("read", false)
        getDisposable = Data.getBooks(context!!, unread)
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(io.reactivex.schedulers.Schedulers.io())
            .subscribeWith(object : DisposableSubscriber<List<Book>>() {
                override fun onNext(books: List<Book>) {
                    updateBooks(books)
                }
                override fun onError(t: Throwable) {
                    getDisposable = null
                }
                override fun onComplete() {
                    getDisposable = null
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = BookAdapter(mutableListOf())
    }

    override fun onDestroy() {
        super.onDestroy()
        getDisposable?.dispose()
        markDisposable?.dispose()
        deleteDisposable?.dispose()
    }

    fun updateBooks(books: List<Book>) {
        if (books.isEmpty()) {
            // empty view
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            (recyclerView.adapter as BookAdapter).updateList(books)
        }
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
        AlertDialog.Builder(context!!)
            .setTitle("Mark As Read")
            .setIcon(if (book.read) R.drawable.ic_book_read else R.drawable.ic_book)
            .setMessage("Marker the current book as having been read?")
            .setPositiveButton("Yes", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    markBook(book, true)
                }
            })
            .setCancelable(true)
            .show()
    }

    var markDisposable: Disposable? = null
    var deleteDisposable: Disposable? = null

    fun markBook(book: Book, bookRead: Boolean) {
        Data.updateBookRead(context!!, book, bookRead)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(@NonNull d: Disposable) {
                    markDisposable = d
                }
                override fun onComplete() {
                    markDisposable = null
                }
                override fun onError(@NonNull e: Throwable) {
                    markDisposable = null
                }
            })
    }

    fun deleteBook(book: Book) {
        Data.deleteBook(context!!, book)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(@NonNull d: Disposable) {
                    deleteDisposable = d
                }
                override fun onComplete() {
                    deleteDisposable = null
                }
                override fun onError(@NonNull e: Throwable) {
                    deleteDisposable = null
                }
            })
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var book: Book? = null
            val title: TextView by bindView(R.id.txtTitle)
            val author: TextView by bindView(R.id.txtAuthor)
            val image: ImageView by bindView(R.id.imgThumbnail)
            val target: com.squareup.picasso.Target
            val menu: ImageView by bindView(R.id.menu)

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
                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        image.scaleType = ImageView.ScaleType.CENTER
                        image.setImageResource(if (book != null && book!!.read) R.drawable.ic_book_read else R.drawable.ic_book)
                    }
                }
                menu.setOnClickListener { showMenu(menu) }
            }

            @SuppressLint("RestrictedApi")
            fun showMenu(view: View) {
                //creating a popup menu
                val popup = PopupMenu(context!!, view)

                val whichMenu = if (arguments!!.getBoolean("read", false))
                    R.menu.menu_book_list_read else R.menu.menu_book_list_unread

                //inflating menu from xml resource
                popup.inflate(whichMenu)

                //val menuItemColor = ContextUtil.getPrimaryTextColor(getContext())
                //PopupMenuUtil.showAndTintPopupMenuIcons(popup, menuItemColor)

                //adding click listener
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_mark_unread -> markBook(book!!, false)
                        R.id.action_mark_read -> markBook(book!!, true)
                        R.id.action_remove -> deleteBook(book!!)
                    }
                    false
                }

                (popup.menu as MenuBuilder).setOptionalIconsVisible(true);

                //displaying the popup
                popup.show()
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
