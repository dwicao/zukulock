package com.tiendnm.zukulock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TapGateTest {

    @Test
    fun `first ever tap is not a duplicate`() {
        assertFalse(TapGate.isDuplicate(now = 5_000L, last = 0L, windowMs = 2_000L))
    }

    @Test
    fun `tap inside window is a duplicate`() {
        assertTrue(TapGate.isDuplicate(now = 5_500L, last = 5_000L, windowMs = 2_000L))
    }

    @Test
    fun `tap at the exact window boundary is not a duplicate`() {
        assertFalse(TapGate.isDuplicate(now = 7_000L, last = 5_000L, windowMs = 2_000L))
    }

    @Test
    fun `tap one ms before boundary is a duplicate`() {
        assertTrue(TapGate.isDuplicate(now = 6_999L, last = 5_000L, windowMs = 2_000L))
    }

    @Test
    fun `backwards clock jump re-enables input`() {
        assertFalse(TapGate.isDuplicate(now = 4_000L, last = 5_000L, windowMs = 2_000L))
    }

    @Test
    fun `same instant is a duplicate`() {
        assertTrue(TapGate.isDuplicate(now = 5_000L, last = 5_000L, windowMs = 2_000L))
    }
}
