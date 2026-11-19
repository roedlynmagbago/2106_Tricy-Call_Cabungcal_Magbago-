package com.example.nasugbutricycleapp

import android.app.Application
import com.example.nasugbutricycleapp.firebase.FirebaseManager

class TricyCallApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseManager.connectToEmulators()
    }
}
