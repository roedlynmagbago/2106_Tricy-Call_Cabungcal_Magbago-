package com.example.draft // Dapat tugma ito sa folder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Siguraduhing activity_login ang tinatawag dito
        setContentView(R.layout.activity_login)
    }
}