package com.example.nasugbutricycleapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nasugbutricycleapp.service.AuthService

class LoginActivity : AppCompatActivity() {

    private var isPassengerSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val cardPassenger = findViewById<LinearLayout>(R.id.cardPassenger)
        val ivPassengerIcon = findViewById<ImageView>(R.id.ivPassengerIcon)
        val cardToda = findViewById<LinearLayout>(R.id.cardToda)
        val ivTodaIcon = findViewById<ImageView>(R.id.ivTodaIcon)
        val tvVerificationInfo = findViewById<TextView>(R.id.tvVerificationInfo)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)

        btnBack?.setOnClickListener {
            finish()
        }

        // Toggle User Type selection (Passenger vs TODA Driver)
        cardPassenger.setOnClickListener {
            if (!isPassengerSelected) {
                isPassengerSelected = true
                cardPassenger.setBackgroundResource(R.drawable.bg_card_active)
                ivPassengerIcon.setBackgroundColor(Color.parseColor("#FFFFFF"))

                cardToda.setBackgroundResource(R.drawable.bg_card_inactive)
                ivTodaIcon.setBackgroundColor(Color.parseColor("#F5F7FA"))

                tvVerificationInfo.text = "Logging in as Verified Passenger (Nasugbu Resident / Guest)"
                etEmail.hint = "passenger@email.com"
            }
        }

        cardToda.setOnClickListener {
            if (isPassengerSelected) {
                isPassengerSelected = false
                cardToda.setBackgroundResource(R.drawable.bg_card_active)
                ivTodaIcon.setBackgroundColor(Color.parseColor("#FFFFFF"))

                cardPassenger.setBackgroundResource(R.drawable.bg_card_inactive)
                ivPassengerIcon.setBackgroundColor(Color.parseColor("#F5F7FA"))

                tvVerificationInfo.text = "Logging in as Verified TODA Driver (Franchise Unit)"
                etEmail.hint = "driver@email.com"
            }
        }

        // Toggle Password Visibility
        var isPasswordVisible = false
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }

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

            AuthService.login(email, password) { success, roleOrMessage ->
                runOnUiThread {
                    btnLogin.isEnabled = true

                    if (success) {
                        Toast.makeText(
                            this,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (roleOrMessage == "DRIVER") {
                            startActivity(
                                Intent(this, DriverActivity::class.java)
                            )
                        } else {
                            startActivity(
                                Intent(this, MapActivity::class.java)
                            )
                        }
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            roleOrMessage,
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
