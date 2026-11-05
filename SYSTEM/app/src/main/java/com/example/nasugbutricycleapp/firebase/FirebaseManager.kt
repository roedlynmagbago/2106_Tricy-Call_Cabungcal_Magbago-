package com.example.nasugbutricycleapp.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun connectToFirestoreEmulator() {
        db.useEmulator("192.168.1.25", 8080)
    }

    fun connectToAuthEmulator() {
        auth.useEmulator("192.168.1.25", 9099)
    }

    fun connectToEmulators() {
        connectToAuthEmulator()
        connectToFirestoreEmulator()
    }
}
