package com.example.nasugbutricycleapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nasugbutricycleapp.service.AuthService
import com.example.nasugbutricycleapp.firebase.FirebaseManager

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseManager.connectToEmulators()

        android.util.Log.d("FIREBASE_CHECK", "Auth emulator: 192.168.1.25:9099")
        android.util.Log.d("FIREBASE_CHECK", "Firestore emulator: 192.168.1.25:8080")

        setContentView(R.layout.login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCreateAccount =
            findViewById<TextView>(R.id.tvCreateAccount)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(
                    this,
                    "Please enter your email and password.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            AuthService.login(email, password) { success, message ->
                runOnUiThread {
                    btnLogin.isEnabled = true

                    if (success) {
                        Toast.makeText(
                            this,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(this, MapActivity::class.java)
                        )
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Open Create Account screen
        tvCreateAccount.setOnClickListener {
            startActivity(
                Intent(this, CreateAccountActivity::class.java)
            )
        }
    }
}
