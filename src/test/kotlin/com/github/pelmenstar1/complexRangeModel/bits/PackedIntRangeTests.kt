package com.github.pelmenstar1.complexRangeModel.bits

import kotlin.test.Test
import kotlin.test.assertEquals

class PackedIntRangeTests {
    @Test
    fun createAndGetTest() {
        fun testCase(start: Int, end: Int) {
            val range = PackedIntRange(start, end)

            assertEquals(start, range.start, "start")
            assertEquals(end, range.endInclusive, "end")
        }

        testCase(0, 0)
        testCase(-1, 0)
        testCase(0, -1)
        testCase(-1, -1)
        testCase(-2, -3)
        testCase(2, 0)
        testCase(0, 2)
    }
}