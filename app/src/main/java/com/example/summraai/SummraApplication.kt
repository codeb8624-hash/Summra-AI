package com.example.summraai

import android.app.Application
import com.example.summraai.data.local.SummraDatabase

class SummraApplication : Application() {
    companion object {
        lateinit var instance: SummraApplication
            private set
    }

    val database: SummraDatabase by lazy { SummraDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
