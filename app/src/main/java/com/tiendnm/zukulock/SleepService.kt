package com.tiendnm.zukulock

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import rikka.shizuku.Shizuku

class SleepService : Service() {

    companion object {
        private const val DEBOUNCE_MS = 2000L
        private const val PREFS_NAME = "zukulock_prefs"
        private const val KEY_LAST_TRIGGER = "last_trigger_millis"

        // In-memory guard for rapid re-launches while the process is alive.
        @Volatile
        private var lastTriggerRealtime: Long = 0L

        // KEYCODE_SLEEP only turns the screen off (no toggle, no wake-up).
        // KEYCODE_POWER (26) toggles, so a fast double-tap is seen by the
        // system as "double-press power" which opens the camera on Pixels.
        private const val KEYCODE_SLEEP = "223"
        private const val KEYCODE_POWER = "26"
    }

    private val REQUEST_CODE: Int = 0

    private fun checkPermission(code: Int): Boolean {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "Shizuku service is not running", Toast.LENGTH_LONG).show()
            return false
        }
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return false
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            return true
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
            return false
        } else {
            // Request the permission
            Shizuku.requestPermission(code)
            return false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isDuplicateTap()) {
            Log.d("ZukuLock", "Ignoring rapid re-tap (debounce)")
            stopSelf()
            return START_NOT_STICKY
        }
        markTriggered()
        val check = checkPermission(REQUEST_CODE)
        if (check) {
            runShellCommand()
        }
        stopSelf()

        return START_NOT_STICKY
    }

    private fun isDuplicateTap(): Boolean {
        val now = android.os.SystemClock.elapsedRealtime()
        val lastInMemory = lastTriggerRealtime
        if (now - lastInMemory < DEBOUNCE_MS) {
            return true
        }
        // Cross-process guard: each tap spawns a new Activity/Service
        // invocation, so also persist the last trigger time.
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastPersisted = prefs.getLong(KEY_LAST_TRIGGER, 0L)
        val wallNow = System.currentTimeMillis()
        if (wallNow - lastPersisted < DEBOUNCE_MS) {
            // Refresh in-memory guard so a burst of taps stays suppressed.
            lastTriggerRealtime = now
            return true
        }
        return false
    }

    private fun markTriggered() {
        lastTriggerRealtime = android.os.SystemClock.elapsedRealtime()
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_TRIGGER, System.currentTimeMillis())
            .apply()
    }

    private fun runShellCommand() {
        try {
            // Prefer SLEEP (223): screen-off only, never wakes the device,
            // so it can never be interpreted as double-press-power -> camera.
            val sleepOk = ShizukuHelper.execute(arrayOf("input", "keyevent", KEYCODE_SLEEP), null, null)
            if (!sleepOk) {
                // Fallback for devices where SLEEP is unsupported.
                Log.w("ShizukuShell", "SLEEP keyevent failed, falling back to POWER")
                ShizukuHelper.execute(arrayOf("input", "keyevent", KEYCODE_POWER), null, null)
            }
        } catch (e: Exception) {
            Log.e("ShizukuShell", "Error running shell command", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}