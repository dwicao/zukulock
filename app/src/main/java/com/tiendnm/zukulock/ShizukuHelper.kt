package com.tiendnm.zukulock

import android.util.Log
import java.lang.reflect.Method
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import rikka.shizuku.ShizukuRemoteProcess

/**
 * Runs shell commands through Shizuku.
 *
 * Shizuku deliberately keeps [rikka.shizuku.Shizuku.newProcess] private, so
 * the call has to go through reflection. Keep the signatures here in sync
 * with the Shizuku API version pinned in gradle/libs.versions.toml.
 */
object ShizukuHelper {

    private const val TAG = "ShizukuHelper"
    private const val WAIT_TIMEOUT_MS = 10_000L

    // Used to drain both pipes of the remote process concurrently.
    private val drainExecutor = Executors.newCachedThreadPool()

    /** Returns true when the command ran and exited with code 0. */
    fun execute(cmd: Array<String>, env: Array<String>?, dir: String?): Boolean {
        return try {
            val process = newProcess(cmd, env, dir) ?: return false
            try {
                // Drain both pipes concurrently: reading stdout first and
                // stderr afterwards can deadlock if stderr fills up while
                // stdout is still being drained.
                drain(process)
                val exited = process.waitForTimeout(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                if (!exited) {
                    Log.e(TAG, "Timed out waiting for: ${cmd.joinToString(" ")}")
                    return false
                }
                val exitCode = process.exitValue()
                if (exitCode != 0) {
                    Log.e(TAG, "Command exited with $exitCode: ${cmd.joinToString(" ")}")
                }
                exitCode == 0
            } finally {
                try {
                    process.destroy()
                } catch (ignored: Exception) {
                    // best effort
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error running command: ${cmd.joinToString(" ")}", e)
            false
        }
    }

    private fun newProcess(
        cmd: Array<String>,
        env: Array<String>?,
        dir: String?
    ): ShizukuRemoteProcess? {
        val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
        val method: Method = shizukuClass.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        val result = method.invoke(null, cmd, env, dir)
        if (result !is ShizukuRemoteProcess) {
            Log.e(TAG, "newProcess returned unexpected type: ${result?.javaClass?.name}")
            return null
        }
        return result
    }

    private fun drain(process: ShizukuRemoteProcess) {
        val stdout = drainExecutor.submit {
            try {
                process.inputStream.bufferedReader().use { it.readText() }
            } catch (ignored: Exception) {
                // stream already closed or process died — nothing to drain
            }
        }
        val stderr = drainExecutor.submit {
            try {
                process.errorStream.bufferedReader().use { it.readText() }
            } catch (ignored: Exception) {
                // stream already closed or process died — nothing to drain
            }
        }
        try {
            stdout.get(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            stderr.get(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Log.w(TAG, "Draining process output did not finish in time", e)
        }
    }
}
