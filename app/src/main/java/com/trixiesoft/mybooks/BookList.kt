package com.trixiesoft.mybooks

import android.os.Bundle
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_book_list.*

class BookList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)
    }
}
