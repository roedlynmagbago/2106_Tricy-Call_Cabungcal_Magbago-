package com.example.nasugbutricycleapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nasugbutricycleapp.service.AuthService

class DriverProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_profile)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val tvDriverName = findViewById<TextView>(R.id.tvDriverName)
        val tvDriverTodaPlate = findViewById<TextView>(R.id.tvDriverTodaPlate)
        val tvActiveYears = findViewById<TextView>(R.id.tvActiveYears)
        val tvMobileNumber = findViewById<TextView>(R.id.tvMobileNumber)
        val tvHomeAddress = findViewById<TextView>(R.id.tvHomeAddress)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)
        val btnEditProfile = findViewById<TextView>(R.id.btnEditProfile)
        val navDashboard = findViewById<LinearLayout>(R.id.navDashboard)

        // Back Button
        ivBack?.setOnClickListener {
            finish()
        }

        // Nav Dashboard
        navDashboard?.setOnClickListener {
            startActivity(Intent(this, DriverActivity::class.java))
            finish()
        }

        // Edit Profile
        btnEditProfile?.setOnClickListener {
            Toast.makeText(this, "Profile editing feature coming soon.", Toast.LENGTH_SHORT).show()
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

        // Load logged-in driver user profile dynamically from Firebase
        loadDriverProfile(
            tvDriverName,
            tvDriverTodaPlate,
            tvMobileNumber,
            tvHomeAddress,
            tvActiveYears,
        )
    }

    private fun loadDriverProfile(
        tvDriverName: TextView,
        tvDriverTodaPlate: TextView,
        tvMobileNumber: TextView,
        tvHomeAddress: TextView,
        tvActiveYears: TextView,
    ) {
        AuthService.getCurrentUser { user, error ->
            runOnUiThread {
                if (user != null) {
                    val displayName = user.name.ifBlank {
                        user.username.ifBlank {
                            user.email.substringBefore("@")
                        }
                    }
                    tvDriverName.text = displayName.ifBlank { "Driver Profile" }

                    val todaInfo = if (user.toda.isNotBlank()) "TODA #${user.toda}" else "Verified TODA Driver"
                    tvDriverTodaPlate.text = todaInfo

                    tvMobileNumber.text = user.phoneNumber.ifBlank { "Not provided" }
                    tvHomeAddress.text = user.address.ifBlank { "Nasugbu, Batangas" }
                    tvActiveYears.text = user.status.ifBlank { "Active" }
                } else if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
