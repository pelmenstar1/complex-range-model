package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertEquals

class PermutationsTests {
    @Test
    fun allPermutationsTest() {
        fun testCase(input: List<Int>, expected: List<Collection<Int>>) {
            val actualSet = input.allPermutations().toSet()
            val expectedSet = expected.toSet()

            assertEquals(expectedSet, actualSet)
        }

        testCase(input = listOf(1), expected = listOf(listOf(1)))
        testCase(
            input = listOf(1, 2),
            expected = listOf(
                listOf(1, 2),
                listOf(2, 1)
            )
        )

        testCase(
            input = listOf(1, 2, 3),
            expected = listOf(
                listOf(1, 2, 3),
                listOf(1, 3, 2),
                listOf(2, 1, 3),
                listOf(2, 3, 1),
                listOf(3, 2, 1),
                listOf(3, 1, 2)
            )
        )
    }
}