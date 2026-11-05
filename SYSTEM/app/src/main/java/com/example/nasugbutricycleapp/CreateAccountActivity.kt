package com.example.nasugbutricycleapp

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nasugbutricycleapp.service.AuthService

class CreateAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword =
            findViewById<EditText>(R.id.etConfirmPassword)

        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnCreateAccount =
            findViewById<Button>(R.id.btnCreateAccount)

        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        tvLogin.setOnClickListener {
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnCreateAccount.setOnClickListener {

            val name = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword =
                etConfirmPassword.text.toString()

            if (
                name.isBlank() ||
                username.isBlank() ||
                email.isBlank() ||
                password.isBlank() ||
                confirmPassword.isBlank()
            ) {
                Toast.makeText(
                    this,
                    "Please fill in all fields.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(
                    this,
                    "Please agree to the Terms and Conditions.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            btnCreateAccount.isEnabled = false

            AuthService.register(
                name = name,
                username = username,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                applyAsDriver = false
            ) { success, message ->

                runOnUiThread {
                    btnCreateAccount.isEnabled = true

                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (success) {
                        finish()
                    }
                }
            }
        }
    }
}