package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class BaseComplexRangeTransitionManagerTests {
    protected abstract fun createComplexRange(ranges: List<IntRange>): IntComplexRange

    private fun createEmptyComplexRange() = createComplexRange(emptyList())

    @Test
    fun createEmptyToEmptyTest() {
        val origin = createEmptyComplexRange()
        val dest = createEmptyComplexRange()

        val transition = createTransition(origin, dest)

        assertTrue(transition.groups().isEmpty())
    }

    @Test
    fun createEmptyToNonEmptyTest() {
        val origin = createEmptyComplexRange()
        val dest = createComplexRange(listOf(0..2, 4..5))
        val actualTransition = createTransition(origin, dest)

        assertGroupsEquals(actualTransition) {
            group {
                insert(0..2)
            }

            group {
                insert(4..5)
            }
        }
    }

    @Test
    fun createNonEmptyToEmptyTest() {
        val origin = createComplexRange(listOf(0..2, 4..5))
        val dest = createEmptyComplexRange()
        val actualTransition = createTransition(origin, dest)

        assertGroupsEquals(actualTransition) {
            group {
                remove(0..2)
            }

            group {
                remove(4..5)
            }
        }
    }

    private fun transitionTestHelper(
        origin: List<IntRange>,
        dest: List<IntRange>,
        maxMoveDist: Int = -1,
        transitionBuild: TransitionBuilder<IntFragmentElement>.() -> Unit
    ) {
        val originComplexRange = createComplexRange(origin)
        val destComplexRange = createComplexRange(dest)

        val actualTransition = createTransition(originComplexRange, destComplexRange, maxMoveDist)

        assertGroupsEquals(actualTransition, transitionBuild)
    }

    @Test
    fun createNonEmptyToNonEmpty_singleGroupTest() {
        // Join + Transform

        transitionTestHelper(
            origin = listOf(1..2, 4..5),
            dest = listOf(2..4)
        ) {
            group {
                join(originRanges = arrayOf(1..2, 4..5), destRange = 1..5)
                transform(origin = 1..5, dest = 2..4)
            }
        }

        transitionTestHelper(
            origin = listOf(1..2, 4..4, 6..6),
            dest = listOf(2..7)
        ) {
            group {
                join(originRanges = arrayOf(1..2, 4..4, 6..6), destRange = 1..6)
                transform(origin = 1..6, dest = 2..7)
            }
        }

        transitionTestHelper(
            origin = listOf(1..1, 3..3),
            dest = listOf(1..3)
        ) {
            group {
                join(originRanges = arrayOf(1..1, 3..3), destRange = 1..3)
            }
        }

        // Join + Transform + Split

        transitionTestHelper(
            origin = listOf(1..2, 4..11),
            dest = listOf(2..4, 7..10)
        ) {
            group {
                join(originRanges = arrayOf(1..2, 4..11), destRange = 1..11)
                transform(origin = 1..11, dest = 2..10)
                split(originRange = 2..10, destRanges = arrayOf(2..4, 7..10))
            }
        }

        // Transform

        transitionTestHelper(
            origin = listOf(1..2),
            dest = listOf(2..3)
        ) {
            group {
                transform(origin = 1..2, dest = 2..3)
            }
        }

        // Ignore same fragments
        transitionTestHelper(
            origin = listOf(1..2),
            dest = listOf(1..2)
        ) {
        }

        // Transform + Split

        transitionTestHelper(
            origin = listOf(1..5),
            dest = listOf(2..2, 4..4)
        ) {
            group {
                transform(origin = 1..5, dest = 2..4)
                split(originRange = 2..4, destRanges = arrayOf(2..2, 4..4))
            }
        }

        // Remove + Insert

        transitionTestHelper(
            origin = listOf(1..2),
            dest = listOf(3..4)
        ) {
            group {
                remove(1..2)
            }

            group {
                insert(3..4)
            }
        }

        transitionTestHelper(
            origin = listOf(1..2),
            dest = listOf(3..4)
        ) {
            group {
                remove(1..2)
            }

            group {
                insert(3..4)
            }
        }

        // Move

        transitionTestHelper(
            origin = listOf(1..2),
            dest = listOf(3..4),
            maxMoveDist = 1
        ) {
            group {
                move(origin = 1..2, dest = 3..4)
            }
        }

        transitionTestHelper(
            origin = listOf(1..2),
            dest = listOf(4..5),
            maxMoveDist = 0
        ) {
            group {
                remove(1..2)
            }

            group {
                insert(4..5)
            }
        }

        transitionTestHelper(
            origin = listOf(1..2),
            dest = listOf(5..6),
            maxMoveDist = 1
        ) {
            group {
                remove(1..2)
            }

            group {
                insert(5..6)
            }
        }
    }

    @Test
    fun createNonEmptyToNonEmpty_multipleGroupsTest() {
        // Ignore same elements
        transitionTestHelper(
            origin = listOf(1..2, 6..7),
            dest = listOf(2..3, 6..7)
        ) {
            group {
                transform(origin = 1..2, dest = 2..3)
            }
        }

        transitionTestHelper(
            origin = listOf(1..2, 6..7, 9..10),
            dest = listOf(2..3, 6..7, 10..11)
        ) {
            group {
                transform(origin = 1..2, dest = 2..3)
            }

            group {
                transform(origin = 9..10, dest = 10..11)
            }
        }
    }

    @Test
    fun consumeElementsForTransformGroupTest() {
        fun createIterator(ranges: List<IntRange>): ComplexRangeFragmentListIterator<IntFragmentElement> {
            return createComplexRange(ranges).fragments().fragmentIterator()
        }

        fun assertConsumed(
            expectedConsumed: Int,
            input: List<IntRange>,
            groupComplexRange: IntComplexRange,
            sourceType: String, testType: String
        ) {
            val resultGroupElements = groupComplexRange.fragments().toTypedArray()

            assertEquals(expectedConsumed, resultGroupElements.size, "$sourceType consumed ($testType)")

            val slicedInput = input.take(expectedConsumed).map { IntRangeFragment(it) }.toTypedArray()
            assertContentEquals(slicedInput, resultGroupElements, "$sourceType elements ($testType)")
        }

        fun testCaseBase(
            origin: List<IntRange>, dest: List<IntRange>,
            expectedOriginConsumed: Int, expectedDestConsumed: Int,
            isForward: Boolean
        ) {
            val testType = if (isForward) "forward" else "backward"

            val originIter = createIterator(origin)
            val destIter = createIterator(dest)

            originIter.moveNext()
            destIter.moveNext()

            val originFirstFrag = originIter.current

            originIter.mark()
            destIter.mark()

            val manager = ComplexRangeTransitionManager.noMove()
            manager.consumeElementsForTransformGroup(originFirstFrag, originIter, destIter)

            val originGroupRange = originIter.subRange()
            val destGroupRange = destIter.subRange()

            assertConsumed(expectedOriginConsumed, origin, originGroupRange, "origin", testType)
            assertConsumed(expectedDestConsumed, dest, destGroupRange, "dest", testType)
        }

        fun testCase(
            origin: List<IntRange>, dest: List<IntRange>,
            expectedOriginConsumed: Int, expectedDestConsumed: Int
        ) {
            // Grouping must be commutative
            testCaseBase(origin, dest, expectedOriginConsumed, expectedDestConsumed, isForward = true)
            testCaseBase(dest, origin, expectedDestConsumed, expectedOriginConsumed, isForward = false)
        }

        testCase(
            origin = listOf(1..3),
            dest = listOf(1..1),
            expectedOriginConsumed = 1,
            expectedDestConsumed = 1
        )

        testCase(
            origin = listOf(1..3),
            dest = listOf(1..1, 3..3),
            expectedOriginConsumed = 1,
            expectedDestConsumed = 2
        )

        testCase(
            origin = listOf(1..4, 6..8),
            dest = listOf(1..1, 3..4, 7..7),
            expectedOriginConsumed = 1,
            expectedDestConsumed = 2
        )

        testCase(
            origin = listOf(1..3, 5..8),
            dest = listOf(1..1, 3..6),
            expectedOriginConsumed = 2,
            expectedDestConsumed = 2
        )

        testCase(
            origin = listOf(1..1, 3..12, 14..16, 18..19),
            dest = listOf(1..3, 6..6, 8..9, 11..14, 16..16, 19..19),
            expectedOriginConsumed = 3,
            expectedDestConsumed = 5
        )

        testCase(
            origin = listOf(1..3, 5..8, 10..11),
            dest = listOf(1..1, 3..6),
            expectedOriginConsumed = 2,
            expectedDestConsumed = 2
        )

        testCase(
            origin = listOf(1..10),
            dest = listOf(0..2, 4..5, 7..11),
            expectedOriginConsumed = 1,
            expectedDestConsumed = 3
        )

        testCase(
            origin = listOf(1..2, 6..7),
            dest = listOf(2..3, 6..7),
            expectedOriginConsumed = 1,
            expectedDestConsumed = 1
        )
    }

    private fun assertGroupsEquals(
        actual: ComplexRangeTransition<IntFragmentElement>,
        expectedBuild: TransitionBuilder<IntFragmentElement>.() -> Unit
    ) {
        val expected = ComplexRangeTransition(expectedBuild)

        assertEquals(expected, actual)
    }

    private fun createTransition(
        origin: IntComplexRange,
        dest: IntComplexRange,
        maxMoveDist: Int = -1
    ): ComplexRangeTransition<IntFragmentElement> {
        return ComplexRangeTransitionManager.withMoveDistance(maxMoveDist).createTransition(origin, dest)
    }
}