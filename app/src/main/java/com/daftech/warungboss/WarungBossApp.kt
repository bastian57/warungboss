package com.daftech.warungboss

import android.app.Application
import com.daftech.warungboss.data.AppDatabase

class WarungBossApp : Application() {
    lateinit var db: AppDatabase
        private set
    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(this)
    }
}
