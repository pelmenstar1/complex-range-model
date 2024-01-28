package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ComplexRangeElementCollectionTests {
    @Test
    fun sizeTest() {
        fun testCase(ranges: Array<IntRange>, expectedSize: Int) {
            val collection = createCollection(ranges)
            val actualSize = collection.size

            assertEquals(expectedSize, actualSize)
        }

        testCase(ranges = emptyArray(), expectedSize = 0)
        testCase(ranges = arrayOf(1..2), expectedSize = 2)
        testCase(ranges = arrayOf(1..2, 4..6), expectedSize = 5)
    }

    @Test
    fun containsTest() {
        fun testCase(ranges: Array<IntRange>, needle: Int, expected: Boolean) {
            val collection = createCollection(ranges)
            val actual = collection.contains(IntFragmentElement(needle))

            assertEquals(expected, actual)
        }

        testCase(ranges = emptyArray(), needle = 0, expected = false)
        testCase(ranges = arrayOf(1..2), needle = 1, expected = true)
        testCase(ranges = arrayOf(1..2), needle = 3, expected = false)
        testCase(ranges = arrayOf(1..2, 5..7), needle = 7, expected = true)
    }

    @Test
    fun iteratorTest() {
        fun testCase(ranges: Array<IntRange>, expectedElements: Array<Int>) {
            val collection = createCollection(ranges)

            // map uses iterator implicitly
            val actualElements = collection.map { it.value }.toTypedArray()

            assertContentEquals(expectedElements, actualElements)
        }

        testCase(ranges = emptyArray(), expectedElements = emptyArray())
        testCase(ranges = arrayOf(1..2), expectedElements = arrayOf(1, 2))
        testCase(ranges = arrayOf(1..2, 5..8), expectedElements = arrayOf(1, 2, 5, 6, 7, 8))
        testCase(ranges = arrayOf(1..2, 4..6, 8..10), expectedElements = arrayOf(1, 2, 4, 5, 6, 8, 9, 10))
    }

    private fun createCollection(ranges: Array<IntRange>) =
        ComplexRangeElementCollection(ranges.map { IntRangeFragment(it) })
}