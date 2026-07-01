package com.example.summraai

import android.app.Application
import com.example.summraai.data.local.SummraDatabase

class SummraApplication : Application() {
    val database: SummraDatabase by lazy { SummraDatabase.getInstance(this) }
}
