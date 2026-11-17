package com.example.nasugbutricycleapp.service

import android.os.Handler
import android.os.Looper
import android.util.Log

object DriverAvailabilityHeartbeat {

    private const val INTERVAL = 60_000L
    private const val TAG = "DriverHeartbeat"
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) {
                return
            }
            DriverAvailabilityService.refreshOnlineStatus { success, message ->
                if (!success) {
                    Log.e(TAG, message)
                }
            }
            if (isRunning) {
                handler.postDelayed(this, INTERVAL)
            }
        }
    }

    // START HEARTBEAT
    fun start() {
        if (isRunning) return
        isRunning = true
        handler.removeCallbacks(heartbeatRunnable)
        // Update immediately, then repeat every 60 seconds.
        heartbeatRunnable.run()
    }

    // STOP HEARTBEAT
    fun stop() {
        isRunning = false
        handler.removeCallbacks(heartbeatRunnable)
    }

    // CHECK HEARTBEAT STATUS
    fun isRunning(): Boolean {
        return isRunning
    }
}
