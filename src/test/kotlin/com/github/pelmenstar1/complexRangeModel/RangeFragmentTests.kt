package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RangeFragmentTests {
    @Test
    fun constructorThrowsOnInvalidArgsTest() {
        assertFailsWith<IllegalArgumentException> { IntRangeFragment(2, 1) }
    }

    @Test
    fun canUniteWithTest() {
        fun testHelper(r1: IntRange, r2: IntRange, expected: Boolean) {
            val actualResult = IntRangeFragment(r1).canUniteWith(IntRangeFragment(r2))

            assertEquals(expected, actualResult)
        }

        testHelper(1..2, 2..3, expected = true)
        testHelper(1..2, 1..2, expected = true)
        testHelper(1..2, 4..5, expected = false)
        testHelper(1..2, 3..4, expected = true)
        testHelper(3..4, 1..2, expected = true)
    }
}