package com.github.pelmenstar1.complexRangeModel.bits

import kotlin.test.Test
import kotlin.test.assertEquals

class BitsTests {
    @Test
    fun nBitsSetTest() {
        fun testCase(n: Int, expected: Long) {
            val actual = nBitsSet(n)

            assertEquals(expected, actual)
        }

        testCase(n = 0, expected = 0b0)
        testCase(n = 1, expected = 0b1)
        testCase(n = 2, expected = 0b11)
        testCase(n = 3, expected = 0b111)
        testCase(n = 64, expected = -1)
    }

    @Test
    fun rangeMaskTest() {
        fun testCase(start: Int, endInclusive: Int, expected: Long) {
            val actual = rangeMask(start, endInclusive)

            assertEquals(expected, actual)
        }

        testCase(start = 0, endInclusive = 0, expected = 0b1)
        testCase(start = 0, endInclusive = 1, expected = 0b11)
        testCase(start = 1, endInclusive = 2, expected = 0b110)
        testCase(start = 3, endInclusive = 5, expected = 0b111000)
        testCase(start = 0, endInclusive = 63, expected = -1)
    }

    @Test
    fun findNextSetIndexTest() {
        fun testCase(words: LongArray, startIndex: Int, expected: Int) {
            val actual = findNextSetIndex(words, startIndex)

            assertEquals(expected, actual)
        }

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000),
            startIndex = 0,
            expected = -1
        )

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000001),
            startIndex = 0,
            expected = 0
        )

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000000),
            startIndex = 0,
            expected = 7
        )

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000001),
            startIndex = 1,
            expected = 7
        )

        testCase(
            words = longArrayOf(
                0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000001,
                0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000000
            ),
            startIndex = 64,
            expected = 71
        )
    }

    @Test
    fun findNextUnsetIndexTest() {
        fun testCase(words: LongArray, startIndex: Int, expected: Int) {
            val actual = findNextUnsetIndex(words, startIndex)

            assertEquals(expected, actual)
        }

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111111UL.toLong()),
            startIndex = 0,
            expected = -1
        )

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111110UL.toLong()),
            startIndex = 0,
            expected = 0
        )

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111111UL.toLong()),
            startIndex = 0,
            expected = 7
        )

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111110UL.toLong()),
            startIndex = 1,
            expected = 7
        )

        testCase(
            words = longArrayOf(
                0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111110UL.toLong(),
                0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111111UL.toLong()
            ),
            startIndex = 64,
            expected = 71
        )
    }
}