package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplexRangeTransitionManagerTests {
    @Test
    fun createEmptyToEmptyTest() {
        val origin = ComplexRange.empty<Int>()
        val dest = ComplexRange.empty<Int>()

        val transition = createTransition(origin, dest)
        assertTrue(transition.groups.isEmpty())
    }

    @Test
    fun createEmptyToNonEmptyTest() {
        val origin = ComplexRange.empty<Int>()
        val dest = IntComplexRange(arrayOf(0..2, 4..5))
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
        val origin = IntComplexRange(arrayOf(0..2, 4..5))
        val dest = ComplexRange.empty<Int>()
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
        origin: Array<IntRange>,
        dest: Array<IntRange>,
        maxMoveDist: Int = -1,
        transitionBuild: TransitionBuilder<Int>.() -> Unit
    ) {
        val originComplexRange = IntComplexRange(origin)
        val destComplexRange = IntComplexRange(dest)

        val actualTransition = createTransition(originComplexRange, destComplexRange, maxMoveDist)

        assertGroupsEquals(actualTransition, transitionBuild)
    }

    @Test
    fun createNonEmptyToNonEmpty_singleGroupTest(){
        // Join + Transform

        transitionTestHelper(
            origin = arrayOf(1..2, 4..5),
            dest = arrayOf(2..4)
        ) {
            group {
                join(originRanges = arrayOf(1..2, 4..5), destRange = 1..5)
                transform(origin = 1..5, dest = 2..4)
            }
        }

        transitionTestHelper(
            origin = arrayOf(1..2, 4..4, 6..6),
            dest = arrayOf(2..7)
        ) {
            group {
                join(originRanges = arrayOf(1..2, 4..4, 6..6), destRange = 1..6)
                transform(origin = 1..6, dest = 2..7)
            }
        }

        transitionTestHelper(
            origin = arrayOf(1..1, 3..3),
            dest = arrayOf(1..3)
        ) {
            group {
                join(originRanges = arrayOf(1..1, 3..3), destRange = 1..3)
            }
        }

        // Join + Transform + Split

        transitionTestHelper(
            origin = arrayOf(1..2, 4..11),
            dest = arrayOf(2..4, 7..10)
        ) {
            group {
                join(originRanges = arrayOf(1..2, 4..11), destRange = 1..11)
                transform(origin = 1..11, dest = 2..10)
                split(originRange = 2..10, destRanges = arrayOf(2..4, 7..10))
            }
        }

        // Transform

        transitionTestHelper(
            origin = arrayOf(1..2),
            dest = arrayOf(2..3)
        ) {
            group {
                transform(origin = 1..2, dest = 2..3)
            }
        }

        // Ignore same fragments
        transitionTestHelper(
            origin = arrayOf(1..2),
            dest = arrayOf(1..2)
        ) {
        }

        // Transform + Split

        transitionTestHelper(
            origin = arrayOf(1..5),
            dest = arrayOf(2..2, 4..4)
        ) {
            group {
                transform(origin = 1..5, dest = 2..4)
                split(originRange = 2..4, destRanges = arrayOf(2..2, 4..4))
            }
        }

        // Remove + Insert

        transitionTestHelper(
            origin = arrayOf(1..2),
            dest = arrayOf(3..4)
        ) {
            group {
                remove(1..2)
            }

            group {
                insert(3..4)
            }
        }

        transitionTestHelper(
            origin = arrayOf(1..2),
            dest = arrayOf(3..4)
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
            origin = arrayOf(1..2),
            dest = arrayOf(3..4),
            maxMoveDist = 0
        ) {
            group {
                transform(origin = 1..2,  dest = 3..4)
            }
        }

        transitionTestHelper(
            origin = arrayOf(1..2),
            dest = arrayOf(3..4),
            maxMoveDist = 1
        ) {
            group {
                transform(origin = 1..2,  dest = 3..4)
            }
        }

        transitionTestHelper(
            origin = arrayOf(1..2),
            dest = arrayOf(4..5),
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
            origin = arrayOf(1..2),
            dest = arrayOf(5..6),
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
            origin = arrayOf(1..2, 6..7),
            dest = arrayOf(2..3, 6..7)
        ) {
            group {
                transform(origin = 1..2, dest = 2..3)
            }
        }

        transitionTestHelper(
            origin = arrayOf(1..2, 6..7, 9..10),
            dest = arrayOf(2..3, 6..7, 10..11)
        ) {
            group {
                transform(origin = 1..2, dest = 2..3)
            }

            group {
                transform(origin = 9..10, dest = 10..11)
            }
        }
    }

    private fun assertGroupsEquals(
        actual: ComplexRangeTransition<Int>,
        expectedBuild: TransitionBuilder<Int>.() -> Unit
    ) {
        val expected = ComplexRangeTransition(expectedBuild)

        assertEquals(expected, actual)
    }

    private fun createTransition(
        origin: ComplexRange<Int>,
        dest: ComplexRange<Int>,
        maxMoveDist: Int = -1
    ): ComplexRangeTransition<Int> {
        return ComplexRangeTransitionManager.intWithMoveDistance(maxMoveDist).createTransition(origin, dest)
    }
}