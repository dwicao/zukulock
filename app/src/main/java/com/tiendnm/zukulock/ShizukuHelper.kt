package com.tiendnm.zukulock

import android.util.Log
import java.lang.reflect.Method
import rikka.shizuku.ShizukuRemoteProcess

object ShizukuHelper {

    /**
     * Runs a shell command via Shizuku and returns true on exit code 0.
     * Kept under a new name so callers can distinguish SLEEP success
     * from failure and decide about the POWER fallback.
     */
    fun execute(cmd: Array<String>, env: Array<String>?, dir: String?): Boolean {
        return try {
            val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
            val newProcessMethod: Method = shizukuClass.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
            newProcessMethod.isAccessible = true
            val result = newProcessMethod.invoke(null, cmd, env, dir)
            if (result is ShizukuRemoteProcess) {
                try {
                    // Drain outputs to avoid blocking on full pipes.
                    try {
                        result.inputStream.bufferedReader().use { it.readText() }
                    } catch (ignored: Exception) {
                    }
                    try {
                        result.errorStream.bufferedReader().use { it.readText() }
                    } catch (ignored: Exception) {
                    }
                    result.waitFor() == 0
                } finally {
                    try {
                        result.destroy()
                    } catch (ignored: Exception) {
                    }
                }
            } else {
                Log.e("ShizukuHelper", "Result is not an instance of ShizukuRemoteProcess")
                false
            }
        } catch (e: Exception) {
            Log.e("ShizukuHelper", "Error running shell command", e)
            false
        }
    }

    fun useNewProcess(cmd: Array<String>, env: Array<String>?, dir: String?){
        if (!execute(cmd, env, dir)) {
            throw Exception("Shizuku command failed: ${cmd.joinToString(" ")}")
        }
    }
}