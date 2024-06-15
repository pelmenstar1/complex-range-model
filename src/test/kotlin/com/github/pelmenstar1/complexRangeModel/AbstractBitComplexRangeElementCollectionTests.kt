package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

abstract class AbstractBitComplexRangeElementCollectionTests {
    @Test
    fun sizeTest() {
        fun testCase(ranges: Array<IntRange>, limitStart: Int, expectedSize: Int) {
            val collection = createCollection(ranges, limitStart)
            val actualSize = collection.size

            assertEquals(expectedSize, actualSize)
        }

        testCase(ranges = emptyArray(), limitStart = 1, expectedSize = 0)
        testCase(ranges = arrayOf(1..2), limitStart = 1, expectedSize = 2)
        testCase(ranges = arrayOf(1..2, 4..6), limitStart = 1, expectedSize = 5)
    }

    @Test
    fun isEmptyTest() {
        fun testCase(ranges: Array<IntRange>, limitStart: Int, expected: Boolean) {
            val collection = createCollection(ranges, limitStart)
            val actual = collection.isEmpty()

            assertEquals(expected, actual)
        }

        testCase(emptyArray(), limitStart = 1, expected = true)
        testCase(arrayOf(1..2), limitStart = 1, expected = false)
    }

    @Test
    fun containsTest() {
        fun testCase(ranges: Array<IntRange>, needle: Int, expected: Boolean) {
            val collection = createCollection(ranges, limitStart = 1)
            val actual = collection.contains(IntFragmentElement(needle))

            assertEquals(expected, actual)
        }

        testCase(ranges = emptyArray(), needle = 0, expected = false)
        testCase(ranges = arrayOf(1..2), needle = 1, expected = true)
        testCase(ranges = arrayOf(1..2), needle = 3, expected = false)
        testCase(ranges = arrayOf(1..2, 5..7), needle = 7, expected = true)
        testCase(ranges = arrayOf(1..64), needle = 64, expected = true)

        if(maxBits() > 64) {
            testCase(ranges = arrayOf(1..maxBits()), needle = maxBits(), expected = true)
        }
    }

    @Test
    fun containsAllTest() {
        fun testCase(ranges: Array<IntRange>, needles: Array<Int>, expected: Boolean) {
            val collection = createCollection(ranges, limitStart = 1)
            val actual = collection.containsAll(needles.map { IntFragmentElement(it) })

            assertEquals(expected, actual)
        }

        testCase(emptyArray(), needles = arrayOf(1), expected = false)
        testCase(arrayOf(1..2), needles = arrayOf(1), expected = true)
        testCase(arrayOf(1..2), needles = arrayOf(1, 2), expected = true)
        testCase(arrayOf(1..2), needles = arrayOf(1), expected = true)
        testCase(arrayOf(1..2, 5..6), needles = arrayOf(1, 6), expected = true)
        testCase(arrayOf(1..2, 5..6), needles = arrayOf(1, 6, 7), expected = false)
        testCase(arrayOf(1..2, 5..64), needles = arrayOf(1, 6, 64), expected = true)
    }

    @Test
    fun iteratorTest() {
        fun testCase(ranges: Array<IntRange>) {
            val collection = createCollection(ranges, limitStart = 1)
            val expectedElements = ranges.flatMap { it }.toTypedArray()

            // map uses iterator implicitly
            val actualElements = collection.map { it.value }.toTypedArray()

            assertContentEquals(expectedElements, actualElements)
        }

        testCase(ranges = emptyArray())
        testCase(ranges = arrayOf(1..2))
        testCase(ranges = arrayOf(1..2, 5..8))
        testCase(ranges = arrayOf(1..2, 4..6, 8..10))
        testCase(ranges = arrayOf(1..2, 4..6, 8..10, 62..64))

        if (maxBits() > 64) {
            testCase(ranges = arrayOf(1..2, 4..6, 8..10, 62..128))
        }
    }

    protected abstract fun createComplexRange(ranges: Array<IntRange>, limitStart: Int): IntComplexRange
    protected abstract fun maxBits(): Int

    private fun createCollection(ranges: Array<IntRange>, limitStart: Int) =
        createComplexRange(ranges, limitStart).elements()
}