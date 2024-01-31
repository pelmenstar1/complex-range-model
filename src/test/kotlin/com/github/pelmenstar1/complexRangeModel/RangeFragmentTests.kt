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

    private fun<T> memberTestHelper(
        r1: IntRange, r2: IntRange,
        expected: T,
        func: (IntRangeFragment, IntRangeFragment) -> T,
    ) {
        val actualResult = func(IntRangeFragment(r1), IntRangeFragment(r2))
        assertEquals(expected, actualResult)
    }

    private fun<T> commutativeMemberTestHelper(
        r1: IntRange, r2: IntRange,
        expected: T,
        func: (IntRangeFragment, IntRangeFragment) -> T,
    ) {
        memberTestHelper(r1, r2, expected, func)
        memberTestHelper(r2, r1, expected, func)
    }

    private fun <T> limitedMemberTestHelper(
        r1: IntRange, r2: IntRange,
        limit: IntRange,
        expected: T,
        func: (RangeFragment<LimitedIntFragmentElement>, RangeFragment<LimitedIntFragmentElement>) -> T
    ) {
        val f1 = LimitedIntRangeFragment(limit, r1)
        val f2 = LimitedIntRangeFragment(limit, r2)

        val actualResult = func(f1, f2)
        assertEquals(expected, actualResult)
    }

    private fun <T> limitedCommutativeMemberTestHelper(
        r1: IntRange, r2: IntRange,
        limit: IntRange,
        expected: T,
        func: (RangeFragment<LimitedIntFragmentElement>, RangeFragment<LimitedIntFragmentElement>) -> T
    ) {
        limitedMemberTestHelper(r1, r2, limit, expected, func)
        limitedMemberTestHelper(r2, r1, limit, expected, func)
    }

    @Test
    fun canUniteWithTest() {
        fun testHelper(r1: IntRange, r2: IntRange, expected: Boolean) {
           commutativeMemberTestHelper(r1, r2, expected, IntRangeFragment::canUniteWith)
        }

        testHelper(1..2, 2..3, expected = true)
        testHelper(1..2, 1..2, expected = true)
        testHelper(1..2, 4..5, expected = false)
        testHelper(1..2, 3..4, expected = true)
    }

    @Test
    fun limitedCanUniteWithTest() {
        fun testHelper(r1: IntRange, r2: IntRange, limit: IntRange, expected: Boolean) {
            limitedCommutativeMemberTestHelper(r1, r2, limit, expected, LimitedIntRangeFragment::canUniteWith)
        }

        testHelper(r1 = 1..5, r2 = 6..7, limit = 1..7, expected = true)
    }

    @Test
    fun overlapsTest() {
        fun testHelper(r1: IntRange, r2: IntRange, expected: Boolean) {
            commutativeMemberTestHelper(r1, r2, expected, IntRangeFragment::overlapsWith)
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
            memberTestHelper(base, needle, expected, IntRangeFragment::containsExclusive)
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
            memberTestHelper(base, needle, expected, IntRangeFragment::containsCompletely)
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
            memberTestHelper(base, needle, expected, IntRangeFragment::leftContains)
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
    fun iteratorThrowsInEndTest() {
        val iter = IntRangeFragment(1, 3).iterator()
        while(iter.hasNext()) {
            iter.next()
        }

        assertFailsWith<NoSuchElementException> {
            iter.next()
        }
    }

    @Test
    fun getRawMoveDistanceToTest() {
        fun testHelper(r1: IntRange, r2: IntRange, expected: Int) {
            commutativeMemberTestHelper(r1, r2, expected, IntRangeFragment::getRawDistanceTo)
        }

        testHelper(1..2, 1..1, expected = 0)
        testHelper(1..2, 3..4, expected = 1)
        testHelper(1..2, 4..5, expected = 2)
    }

    @Test
    fun toStringTest() {
        val frag = IntRangeFragment(1, 2)
        val actual = frag.toString()
        val expected = "[1, 2]"

        assertEquals(expected, actual)
    }
}