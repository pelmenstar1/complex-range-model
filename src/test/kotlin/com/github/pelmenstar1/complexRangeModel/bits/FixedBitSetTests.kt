package com.github.pelmenstar1.complexRangeModel.bits

import kotlin.test.Test
import kotlin.test.assertEquals

class FixedBitSetTests {
    @Test
    fun findNextSetIndexTest() {
        fun testCase(words: LongArray, startIndex: Int, expected: Int) {
            val actual = FixedBitSet(words).findNextSetBitIndex(startIndex)

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
            val actual = FixedBitSet(words).findNextUnsetBitIndex(startIndex)

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