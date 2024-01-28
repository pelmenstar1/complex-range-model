package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RangeFragmentTests {
    @Test
    fun constructorThrowsOnInvalidArgsTest() {
        assertFailsWith<IllegalArgumentException> { IntRangeFragment(2, 1) }
    }

    private fun predicateMemberTestHelper(
        r1: IntRange, r2: IntRange,
        expected: Boolean,
        predicate: (IntRangeFragment, IntRangeFragment) -> Boolean,
    ) {
        val actualResult = predicate(IntRangeFragment(r1), IntRangeFragment(r2))
        assertEquals(expected, actualResult)
    }

    private fun commutativePredicateMemberTestHelper(
        r1: IntRange, r2: IntRange,
        expected: Boolean,
        predicate: (IntRangeFragment, IntRangeFragment) -> Boolean,
    ) {
        predicateMemberTestHelper(r1, r2, expected, predicate)
        predicateMemberTestHelper(r2, r1, expected, predicate)
    }

    @Test
    fun canUniteWithTest() {
        fun testHelper(r1: IntRange, r2: IntRange, expected: Boolean) {
           commutativePredicateMemberTestHelper(r1, r2, expected, IntRangeFragment::canUniteWith)
        }

        testHelper(1..2, 2..3, expected = true)
        testHelper(1..2, 1..2, expected = true)
        testHelper(1..2, 4..5, expected = false)
        testHelper(1..2, 3..4, expected = true)
    }

    @Test
    fun overlapsTest() {
        fun testHelper(r1: IntRange, r2: IntRange, expected: Boolean) {
            commutativePredicateMemberTestHelper(r1, r2, expected, IntRangeFragment::overlapsWith)
        }

        testHelper(1..2, 2..3, expected = true)
        testHelper(1..2, 1..2, expected = true)
        testHelper(0..3, 1..2, expected = true)
        testHelper(0..2, 3..4, expected = false)
        testHelper(0..2, 4..5, expected = false)
    }

    @Test
    fun containsExclusiveTest() {
        fun testHelper(base: IntRange, needle: IntRange, expected: Boolean) {
            predicateMemberTestHelper(base, needle, expected, IntRangeFragment::containsExclusive)
        }

        testHelper(base = 1..5, needle = 2..2, expected = true)
        testHelper(base = 1..5, needle = 2..4, expected = true)
        testHelper(base = 1..5, needle = 1..5, expected = false)
        testHelper(base = 1..5, needle = 1..4, expected = false)
        testHelper(base = 1..5, needle = 2..5, expected = false)
        testHelper(base = 1..5, needle = 5..6, expected = false)
    }

    @Test
    fun containsCompletelyTest() {
        fun testHelper(base: IntRange, needle: IntRange, expected: Boolean) {
            predicateMemberTestHelper(base, needle, expected, IntRangeFragment::containsCompletely)
        }

        testHelper(base = 1..5, needle = 2..2, expected = true)
        testHelper(base = 1..5, needle = 2..4, expected = true)
        testHelper(base = 1..5, needle = 1..5, expected = true)
        testHelper(base = 1..5, needle = 1..4, expected = true)
        testHelper(base = 1..5, needle = 2..5, expected = true)
        testHelper(base = 1..5, needle = 5..6, expected = false)
    }

    @Test
    fun leftContainsTest() {
        fun testHelper(base: IntRange, needle: IntRange, expected: Boolean) {
            predicateMemberTestHelper(base, needle, expected, IntRangeFragment::leftContains)
        }

        testHelper(base = 1..5, needle = 1..5, expected = true)
        testHelper(base = 1..5, needle = 0..3, expected = true)
        testHelper(base = 4..5, needle = 1..2, expected = false)
        testHelper(base = 2..5, needle = 4..7, expected = false)
        testHelper(base = 2..5, needle = 7..8, expected = false)
    }

    @Test
    fun iteratorTest() {
        fun testHelper(range: IntRange, expected: IntArray) {
            val frag = IntRangeFragment(range)

            // map uses iterator implicitly
            val actual = frag.map { it.value }.toIntArray()

            assertContentEquals(expected, actual)
        }

        testHelper(1..1, expected = intArrayOf(1))
        testHelper(0..1, expected = intArrayOf(0, 1))
        testHelper(0..2, expected = intArrayOf(0, 1, 2))
    }

    @Test
    fun toStringTest() {
        val frag = IntRangeFragment(1, 2)
        val actual = frag.toString()
        val expected = "[1, 2]"

        assertEquals(expected, actual)
    }
}