package com.example.nasugbutricycleapp.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun connectToFirestoreEmulator() {
        db.useEmulator("10.0.2.2", 8080)
    }
}
