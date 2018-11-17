package com.trixiesoft.mybooks

import android.app.Application
import com.squareup.picasso.Picasso

class MyBookApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .loggingEnabled(true)
                .build()
        )
    }
}