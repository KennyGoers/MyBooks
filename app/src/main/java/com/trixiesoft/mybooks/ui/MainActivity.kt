package com.trixiesoft.mybooks.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.trixiesoft.mybooks.R
import com.trixiesoft.mybooks.db.Book
import com.trixiesoft.mybooks.db.Data
import com.trixiesoft.mybooks.utils.bindView
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity(), FindBookFragment.FindBookInterface {

    private val addButton: FloatingActionButton by bindView(R.id.add_book)
    private val progress: ProgressBar by bindView(R.id.progress)
    private val tabLayout: TabLayout by bindView(R.id.tab_layout)
    private val viewPager: ViewPager by bindView(R.id.view_pager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addButton.setOnClickListener {
            openFind()
        }

        val adapter = TabAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_book)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_read)
    }

    private fun openFind() {
        val oldFragment = supportFragmentManager.findFragmentByTag("find_book")
        if (oldFragment != null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.no_change)
                .show(oldFragment)
                .commitNow()
        } else {
            val fragment = FindBookFragment()
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.no_change)
                .add(R.id.find_book_fragment, fragment, "find_book")
                .commitNow()
        }
    }

    override fun bookFound(book: Book) {
        closeFind()
        addBook(book)
    }

    private fun closeFind() {
        val findFragment = supportFragmentManager.findFragmentByTag("find_book")
        if (findFragment != null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.no_change, R.anim.slide_down)
                .hide(findFragment)
                .commitNow()
        }
    }

    override fun cancelFind() {
        closeFind()
    }

    fun error(error: Throwable) {
    }

    fun busy(busy: Boolean) {
        progress.visibility = if (busy) View.VISIBLE else View.GONE
    }

    private fun addBook(book: Book) {
        // don't bother with a busy display, too short to notice
        Data.addBook(this, book)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(@NonNull d: Disposable) {
                    addDisposable = d
                }
                override fun onComplete() {
                    addDisposable = null
                }
                override fun onError(@NonNull e: Throwable) {
                    addDisposable = null
                }
            })
    }

    override fun onStop() {
        super.onStop()
        addDisposable?.dispose()
    }

    private var addDisposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        addDisposable?.dispose()
    }

    override fun onBackPressed() {
        val findFrag = supportFragmentManager.findFragmentByTag("find_book")
        if (findFrag != null && findFrag.isVisible) {
            closeFind()
            return
        }
        super.onBackPressed()
    }

    class TabAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment? = when (position) {
            0 -> BookListFragment.newInstance(false)
            1 -> BookListFragment.newInstance(true)
            else -> null
        }

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            0 -> "Unread"
            1 -> "Read"
            else -> ""
        }

        override fun getCount(): Int = 2
    }
}
