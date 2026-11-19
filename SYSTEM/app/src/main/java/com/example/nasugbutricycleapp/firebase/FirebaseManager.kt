package com.example.nasugbutricycleapp.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    // Set USE_EMULATOR = false to connect directly to Live Firebase Cloud (Works anywhere/on physical devices)
    // Set USE_EMULATOR = true only if running local Firebase Emulator on PC (firebase emulators:start)
    private const val USE_EMULATOR = false
    private const val EMULATOR_HOST = "192.168.1.6"

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun connectToFirestoreEmulator() {
        try {
            db.useEmulator(EMULATOR_HOST, 8080)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Firestore Emulator", e)
        }
    }

    fun connectToAuthEmulator() {
        try {
            auth.useEmulator(EMULATOR_HOST, 9099)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Auth Emulator", e)
        }
    }

    fun connectToEmulators() {
        if (USE_EMULATOR) {
            connectToAuthEmulator()
            connectToFirestoreEmulator()
        }
    }
}
