package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertEquals

class IteratorsTests {
    @Test
    fun sequenceEqualsTest() {
        fun testCase(first: Array<Int>, second: Array<Int>, expected: Boolean) {
            val actual = first.asIterable().sequenceEquals(second.asIterable())

            assertEquals(expected, actual)
        }

        testCase(first = emptyArray(), second = emptyArray(), expected = true)
        testCase(first = arrayOf(0), second = emptyArray(), expected = false)
        testCase(first = emptyArray(), second = arrayOf(0), expected = false)
        testCase(first = arrayOf(0), second = arrayOf(0), expected = true)
        testCase(first = arrayOf(0), second = arrayOf(1), expected = false)
        testCase(first = arrayOf(0, 1), second = arrayOf(0, 1), expected = true)
        testCase(first = arrayOf(0, 1), second = arrayOf(0), expected = false)
        testCase(first = arrayOf(0), second = arrayOf(0, 1), expected = false)
    }
}