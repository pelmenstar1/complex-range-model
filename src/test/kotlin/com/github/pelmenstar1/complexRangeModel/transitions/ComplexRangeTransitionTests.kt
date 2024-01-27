package com.github.pelmenstar1.complexRangeModel.transitions

import kotlin.test.Test
import kotlin.test.assertEquals

class ComplexRangeTransitionTests {
    @Test
    fun reversedTest() {
        fun testCase(
            initial: TransitionBuilder<Int>.() -> Unit,
            reversed: TransitionBuilder<Int>.() -> Unit
        ) {
            val initialTransition = ComplexRangeTransition(initial)

            val expectedReversedTransition = ComplexRangeTransition(reversed)
            val actualReversedTransition = initialTransition.reversed()

            assertEquals(expectedReversedTransition, actualReversedTransition)
        }

        testCase(
            initial = {
                group {
                    insert(1..2)
                    transform(5..6, 6..7)
                }
            },
            reversed = {
                group {
                    transform(6..7, 5..6)
                    remove(1..2)
                }
            }
        )

        testCase(
            initial = {
                group {
                    insert(1..2)
                    transform(5..6, 6..7)
                }

                group {
                    join(originRanges = arrayOf(10..11, 13..15), destRange = 10..15)
                    transform(5..6, 6..7)
                }
            },
            reversed = {
                group {
                    transform(6..7, 5..6)
                    remove(1..2)
                }

                group {
                    transform(6..7, 5..6)
                    split(originRange = 10..15, destRanges = arrayOf(10..11, 13..15))
                }
            }
        )
    }
}