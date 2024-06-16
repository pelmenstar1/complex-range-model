package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractBitComplexRangeTests : BaseComplexRangeTests() {
    @Test
    fun equalsToBitIntComplexRangeTest() {
        fun testCase(fragments: Array<IntRange>, otherFragments: Array<IntRange>, expected: Boolean) {
            val complexRange = createComplexRange(fragments)
            val otherComplexRange = BitIntComplexRange(limitStart = 1, limitEnd = 64) {
                otherFragments.forEach { fragment(it) }
            }

            val actual = complexRange == otherComplexRange
            assertEquals(expected, actual)
        }

        val fragments0 = emptyArray<IntRange>()
        val fragments1 = arrayOf(1..2)
        val fragments2 = arrayOf(1..2, 5..7)
        val fragments3 = arrayOf(2..3)
        val fragments4 = arrayOf(2..60)

        testCase(fragments0, fragments0, expected = true)
        testCase(fragments1, fragments1, expected = true)
        testCase(fragments1, fragments2, expected = false)
        testCase(fragments2, fragments1, expected = false)
        testCase(fragments2, fragments3, expected = false)
        testCase(fragments2, fragments2, expected = true)
        testCase(fragments4, fragments4, expected = true)
        testCase(fragments3, fragments4, expected = false)
    }

    @Test
    fun equalsToBitIntLongComplexRangeTest() {
        fun testCase(fragments: Array<IntRange>, otherFragments: Array<IntRange>, expected: Boolean) {
            val complexRange = createComplexRange(fragments)
            val otherComplexRange = BitLongIntComplexRange(limitStart = 1) {
                otherFragments.forEach { fragment(it) }
            }

            val actual = complexRange == otherComplexRange
            assertEquals(expected, actual)
        }

        val fragments0 = emptyArray<IntRange>()
        val fragments1 = arrayOf(1..2)
        val fragments2 = arrayOf(1..2, 5..7)
        val fragments3 = arrayOf(2..3)
        val fragments4 = arrayOf(2..60)

        testCase(fragments0, fragments0, expected = true)
        testCase(fragments1, fragments1, expected = true)
        testCase(fragments1, fragments2, expected = false)
        testCase(fragments2, fragments1, expected = false)
        testCase(fragments2, fragments3, expected = false)
        testCase(fragments2, fragments2, expected = true)
        testCase(fragments4, fragments4, expected = true)
        testCase(fragments3, fragments4, expected = false)
    }

    @Test
    fun equalsToGenericComplexRangeTest() {
        fun testCase(fragments: Array<IntRange>, otherFragments: Array<IntRange>, expected: Boolean) {
            val complexRange = createComplexRange(fragments)
            val otherComplexRange = IntComplexRange(otherFragments)

            val actual = complexRange == otherComplexRange
            assertEquals(expected, actual)
        }

        val fragments0 = emptyArray<IntRange>()
        val fragments1 = arrayOf(1..2)
        val fragments2 = arrayOf(1..2, 5..7)
        val fragments3 = arrayOf(2..3)
        val fragments4 = arrayOf(2..60)

        testCase(fragments0, fragments0, expected = true)
        testCase(fragments1, fragments1, expected = true)
        testCase(fragments1, fragments2, expected = false)
        testCase(fragments2, fragments1, expected = false)
        testCase(fragments2, fragments3, expected = false)
        testCase(fragments2, fragments2, expected = true)
        testCase(fragments4, fragments4, expected = true)
        testCase(fragments3, fragments4, expected = false)
    }
}