package com.example.nasugbutricycleapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nasugbutricycleapp.service.AuthService

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName = findViewById<TextView>(R.id.tv_name)
        val tvVerified = findViewById<TextView>(R.id.tv_verified)
        val tvContact = findViewById<TextView>(R.id.tv_contact)
        val tvMember = findViewById<TextView>(R.id.tv_member)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navBookRide = findViewById<LinearLayout>(R.id.navBookRide)

        // Navigation
        navHome?.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
            finish()
        }

        navBookRide?.setOnClickListener {
            startActivity(Intent(this, BookRideActivity::class.java))
            finish()
        }

        // Log Out
        btnLogout?.setOnClickListener {
            AuthService.logout()
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Load logged-in passenger profile dynamically from Firebase
        loadPassengerProfile(tvName, tvVerified, tvContact, tvMember)
    }

    private fun loadPassengerProfile(
        tvName: TextView,
        tvVerified: TextView,
        tvContact: TextView,
        tvMember: TextView,
    ) {
        AuthService.getCurrentUser { user, error ->
            runOnUiThread {
                if (user != null) {
                    val displayName = user.name.ifBlank {
                        user.username.ifBlank {
                            user.email.substringBefore("@")
                        }
                    }
                    tvName.text = displayName.ifBlank { "Passenger Profile" }
                    tvVerified.text = if (user.status == "ACTIVE") "Verified Passenger" else "Passenger Account"

                    val phoneStr = user.phoneNumber.ifBlank { "No phone number" }
                    val emailStr = user.email.ifBlank { "" }
                    tvContact.text = if (emailStr.isNotBlank()) "$phoneStr • $emailStr" else phoneStr

                    val addressStr = user.address.ifBlank { "Nasugbu Resident" }
                    tvMember.text = "Member • $addressStr"
                } else if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
