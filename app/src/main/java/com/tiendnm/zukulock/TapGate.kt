package com.tiendnm.zukulock

/**
 * Pure tap-vs-noise decision, kept free of Android dependencies so it can be
 * unit tested on the JVM.
 */
internal object TapGate {

    /**
     * @return true when [now] falls within [windowMs] after [last], meaning
     * the tap should be ignored. A backwards wall-clock jump ([now] before
     * [last]) re-enables input rather than locking the user out forever.
     */
    fun isDuplicate(now: Long, last: Long, windowMs: Long): Boolean =
        now - last in 0 until windowMs
}
