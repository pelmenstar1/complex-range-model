package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.IntComplexRange
import com.github.pelmenstar1.complexRangeModel.IntFragmentElement
import com.github.pelmenstar1.complexRangeModel.IntRangeFragment
import kotlin.test.Test
import kotlin.test.assertEquals

class TransitionOperationTests {
    private fun assertEfficiencyLevel(expected: Int, createOp: () -> TransitionOperation<IntFragmentElement>) {
        val op = createOp()
        val actual = op.efficiencyLevel()

        assertEquals(expected, actual)
    }

    @Test
    fun transformEfficiencyLevelTest() {
        fun testCase(origin: IntRange, dest: IntRange, expected: Int) {
            assertEfficiencyLevel(expected) {
                TransitionOperation.Transform(IntRangeFragment(origin), IntRangeFragment(dest))
            }
        }

        testCase(origin = 1..2, dest = 1..3, expected = 1)
        testCase(origin = 1..2, dest = 0..3, expected = 2)
    }

    @Test
    fun moveEfficiencyLevelTest() {
        fun testCase(origin: IntRange, dest: IntRange, expected: Int) {
            assertEfficiencyLevel(expected) {
                TransitionOperation.Move(IntRangeFragment(origin), IntRangeFragment(dest))
            }
        }

        testCase(origin = 1..2, dest = 3..4, expected = 2)
        testCase(origin = 1..2, dest = 5..6, expected = 4)
    }

    @Test
    fun insertEfficiencyLevelTest() {
        assertEfficiencyLevel(expected = 2) {
            TransitionOperation.Insert(IntRangeFragment(1..2))
        }
    }

    @Test
    fun removeEfficiencyLevelTest() {
        assertEfficiencyLevel(expected = 2) {
            TransitionOperation.Remove(IntRangeFragment(1..2))
        }
    }

    @Test
    fun joinEfficiencyLevelTest() {
        fun testCase(origins: Array<IntRange>, dest: IntRange, expected: Int) {
            assertEfficiencyLevel(expected) {
                TransitionOperation.Join(IntComplexRange(origins), IntRangeFragment(dest))
            }
        }

        testCase(origins = arrayOf(1..2, 4..5), dest = 1..5, expected = 1)
        testCase(origins = arrayOf(1..2, 4..5, 7..8), dest = 1..8, expected = 2)
    }

    @Test
    fun splitEfficiencyLevelTest() {
        fun testCase(origin: IntRange, dests: Array<IntRange>, expected: Int) {
            assertEfficiencyLevel(expected) {
                TransitionOperation.Split(IntRangeFragment(origin), IntComplexRange(dests))
            }
        }

        testCase(origin = 1..5, dests = arrayOf(1..2, 4..5), expected = 1)
        testCase(origin = 1..8, dests = arrayOf(1..2, 4..5, 7..8), expected = 2)
    }
}