package com.tiendnm.zukulock

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import rikka.shizuku.Shizuku

/**
 * Transparent, invisible trampoline activity.
 *
 * Tapping the launcher icon routes straight here; the activity checks the
 * Shizuku permission, fires `input keyevent 223` (KEYCODE_SLEEP) to lock the
 * screen, and finishes itself without ever showing UI. When the permission
 * hasn't been granted yet it requests it and waits for the result before
 * firing the command, so a first-time tap still works.
 */
class MainActivity : Activity() {

    companion object {
        private const val TAG = "ZukuLock"
        private const val REQUEST_PERMISSION_CODE = 1000
        private const val DEBOUNCE_MS = 2_000L
        private const val PREFS_NAME = "zukulock_prefs"
        private const val KEY_LAST_TRIGGER = "last_trigger_wall_millis"

        // KEYCODE_SLEEP only turns the screen off (no toggle, no wake-up).
        // KEYCODE_POWER (26) toggles, so a fast double-tap is seen by the
        // system as "double-press power" which opens the camera on Pixels.
        private const val KEYCODE_SLEEP = "223"
        private const val KEYCODE_POWER = "26"

        // Safety net: never leave the process waiting for a permission
        // result that will never arrive.
        private const val PERMISSION_TIMEOUT_MS = 30_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var waitingForPermissionResult = false

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode != REQUEST_PERMISSION_CODE) return@OnRequestPermissionResultListener
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                lockScreen()
            } else {
                toast(getString(R.string.toast_permission_denied))
            }
            finishCleanly()
        }

    private val permissionTimeout = Runnable {
        if (waitingForPermissionResult) {
            toast(getString(R.string.toast_permission_timeout))
            finishCleanly()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDebounced()) {
            Log.d(TAG, "Ignoring rapid re-tap (debounce)")
            finishAndRemoveTask()
            return
        }

        if (!Shizuku.pingBinder()) {
            toast(getString(R.string.toast_shizuku_not_running))
            finishAndRemoveTask()
            return
        }
        if (Shizuku.isPreV11()) {
            toast(getString(R.string.toast_shizuku_too_old))
            finishAndRemoveTask()
            return
        }

        when {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                lockScreen()
                finishCleanly()
            }

            Shizuku.shouldShowRequestPermissionRationale() -> {
                // User previously chose "Deny and don't ask again".
                toast(getString(R.string.toast_permission_permanently_denied))
                finishAndRemoveTask()
            }

            else -> {
                waitingForPermissionResult = true
                Shizuku.addRequestPermissionResultListener(permissionListener)
                handler.postDelayed(permissionTimeout, PERMISSION_TIMEOUT_MS)
                Shizuku.requestPermission(REQUEST_PERMISSION_CODE)
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(permissionTimeout)
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun isDebounced(): Boolean {
        val last = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getLong(KEY_LAST_TRIGGER, 0L)
        return TapGate.isDuplicate(System.currentTimeMillis(), last, DEBOUNCE_MS)
    }

    private fun markTriggered() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_TRIGGER, System.currentTimeMillis())
            .commit()
    }

    private fun lockScreen() {
        markTriggered()
        val sleepOk = ShizukuHelper.execute(arrayOf("input", "keyevent", KEYCODE_SLEEP), null, null)
        if (!sleepOk) {
            // Fallback for devices where SLEEP is unsupported.
            Log.w(TAG, "SLEEP keyevent failed, falling back to POWER")
            ShizukuHelper.execute(arrayOf("input", "keyevent", KEYCODE_POWER), null, null)
        }
    }

    private fun finishCleanly() {
        waitingForPermissionResult = false
        handler.removeCallbacks(permissionTimeout)
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        finishAndRemoveTask()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
