package com.example.nasugbutricycleapp

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nasugbutricycleapp.service.AuthService

class CreateAccountActivity : AppCompatActivity() {

    private var isPassengerSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val cardPassenger = findViewById<LinearLayout>(R.id.cardPassenger)
        val ivPassengerIcon = findViewById<ImageView>(R.id.ivPassengerIcon)
        val cardToda = findViewById<LinearLayout>(R.id.cardToda)
        val ivTodaIcon = findViewById<ImageView>(R.id.ivTodaIcon)
        val tvVerificationInfo = findViewById<TextView>(R.id.tvVerificationInfo)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)

        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)

        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        tvLogin.setOnClickListener {
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }

        // Toggle User Type Selection
        cardPassenger.setOnClickListener {
            if (!isPassengerSelected) {
                isPassengerSelected = true
                cardPassenger.setBackgroundResource(R.drawable.bg_card_active)
                ivPassengerIcon.setBackgroundColor(Color.parseColor("#FFFFFF"))

                cardToda.setBackgroundResource(R.drawable.bg_card_inactive)
                ivTodaIcon.setBackgroundColor(Color.parseColor("#F5F7FA"))

                tvVerificationInfo.text = "Registering as Verified Passenger (Nasugbu Resident / Guest)"
            }
        }

        cardToda.setOnClickListener {
            if (isPassengerSelected) {
                isPassengerSelected = false
                cardToda.setBackgroundResource(R.drawable.bg_card_active)
                ivTodaIcon.setBackgroundColor(Color.parseColor("#FFFFFF"))

                cardPassenger.setBackgroundResource(R.drawable.bg_card_inactive)
                ivPassengerIcon.setBackgroundColor(Color.parseColor("#F5F7FA"))

                tvVerificationInfo.text = "Registering as Verified TODA Driver (Franchise Unit)"
            }
        }

        // Password visibility toggle
        var isPasswordVisible = false
        ivShowPassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnCreateAccount.setOnClickListener {

            val name = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

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
                applyAsDriver = !isPassengerSelected
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
