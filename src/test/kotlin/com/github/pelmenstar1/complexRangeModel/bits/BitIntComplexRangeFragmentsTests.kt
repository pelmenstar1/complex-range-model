package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BitIntComplexRangeFragmentsTests {
    @Test
    fun sizeTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, expected: Int) {
            propertyTestRepeat(ranges, limitRange, { size }, expected)
        }

        testCase(ranges = emptyList(), limitRange = 1..100, expected = 0)
        testCase(ranges = listOf(2..5), limitRange = 1..100, expected = 1)
        testCase(ranges = listOf(2..5, 7..10), limitRange = 1..100, expected = 2)
        testCase(ranges = listOf(2..5, 65..70), limitRange = 1..100, expected = 2)
    }

    @Test
    fun isEmptyTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, expected: Boolean) {
            propertyTest(ranges, limitRange, { isEmpty() }, expected)
        }

        testCase(ranges = emptyList(), limitRange = 1..10, expected = true)
        testCase(ranges = listOf(2..5), limitRange = 1..100, expected = false)
        testCase(ranges = listOf(2..5, 7..10), limitRange = 1..100, expected = false)
        testCase(ranges = listOf(2..5, 65..70), limitRange = 1..100, expected = false)
    }

    @Test
    fun getTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, index: Int) {
            propertyTest(ranges, limitRange, { get(index).toIntRange() }, ranges[index])
        }

        testCase(ranges = listOf(1..2), limitRange = 1..100, index = 0)
        testCase(ranges = listOf(1..2, 5..6), limitRange = 1..100, index = 0)
        testCase(ranges = listOf(1..2, 5..6), limitRange = 1..100, index = 1)
        testCase(ranges = listOf(1..2, 5..6, 67..100), limitRange = 1..100, index = 2)
    }

    @Test
    fun getFailsOnInvalidIndexTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, index: Int) {
            propertyFailsTest<IndexOutOfBoundsException>(ranges, limitRange) { get(index) }
        }

        testCase(ranges = emptyList(), limitRange = 1..100, index = 0)
        testCase(ranges = emptyList(), limitRange = 1..100, index = -1)
        testCase(ranges = listOf(1..10), limitRange = 1..100, index = 2)
    }

    @Test
    fun lastTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange) {
            propertyTest(ranges, limitRange, { last().toIntRange() }, ranges.last())
        }

        testCase(ranges = listOf(1..10), limitRange = 1..10)
        testCase(ranges = listOf(1..10, 12..30), limitRange = 1..40)
        testCase(ranges = listOf(1..10, 60..70), limitRange = 1..100)
    }

    @Test
    fun lastFailsIfEmptyTest() {
        propertyFailsTest<IndexOutOfBoundsException>(ranges = emptyList(), limitRange = 1..100) { last() }
    }

    @Test
    fun containsTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, target: IntRange) {
            propertyTest(ranges, limitRange, { contains(IntRangeFragment(target)) }, target in ranges)
        }

        testCase(ranges = listOf(), limitRange = 1..10, target = 1..10)
        testCase(ranges = listOf(), limitRange = 1..10, target = 0..12)
        testCase(ranges = listOf(2..5), limitRange = 1..10, target = 0..12)
        testCase(ranges = listOf(2..5), limitRange = 1..10, target = 2..5)
        testCase(ranges = listOf(2..5, 60..70), limitRange = 1..100, target = 60..70)
        testCase(ranges = listOf(2..5, 60..70), limitRange = 1..100, target = 59..70)
        testCase(ranges = listOf(2..5, 60..70), limitRange = 1..100, target = 60..69)
    }

    @Test
    fun includesTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, target: IntRange, expected: Boolean) {
            propertyTest(ranges, limitRange, { includes(IntRangeFragment(target)) }, expected)
        }

        testCase(ranges = listOf(), limitRange = 1..10, target = 1..10, expected = false)
        testCase(ranges = listOf(), limitRange = 1..10, target = 0..12, expected = false)
        testCase(ranges = listOf(2..5), limitRange = 1..10, target = 0..12, expected = false)
        testCase(ranges = listOf(2..5), limitRange = 1..10, target = 2..5, expected = true)
        testCase(ranges = listOf(2..5, 60..70), limitRange = 1..100, target = 60..70, expected = true)
        testCase(ranges = listOf(2..5, 60..70), limitRange = 1..100, target = 59..70, expected = false)
        testCase(ranges = listOf(2..5, 60..70), limitRange = 1..100, target = 60..69, expected = true)
        testCase(ranges = listOf(2..5, 60..70), limitRange = 1..100, target = 65..66, expected = true)
    }

    @Test
    fun indexOfTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, target: IntRange) {
            propertyTest(ranges, limitRange, { indexOf(IntRangeFragment(target)) }, ranges.indexOf(target))
        }

        testCase(ranges = listOf(), limitRange = 1..10, target = 1..2)
        testCase(ranges = listOf(1..1), limitRange = 1..10, target = 0..2)
        testCase(ranges = listOf(1..2, 4..6), limitRange = 1..10, target = 1..2)
        testCase(ranges = listOf(1..2, 4..6), limitRange = 1..10, target = 4..6)
        testCase(ranges = listOf(1..2, 60..70), limitRange = 1..100, target = 60..70)
    }

    @Test
    fun lastIndexOfTest() {
        fun testCase(ranges: List<IntRange>, limitRange: IntRange, target: IntRange) {
            propertyTest(ranges, limitRange, { lastIndexOf(IntRangeFragment(target)) }, ranges.lastIndexOf(target))
        }

        testCase(ranges = listOf(), limitRange = 1..10, target = 1..2)
        testCase(ranges = listOf(1..1), limitRange = 1..10, target = 0..2)
        testCase(ranges = listOf(1..2, 4..6), limitRange = 1..10, target = 1..2)
        testCase(ranges = listOf(1..2, 4..6), limitRange = 1..10, target = 4..6)
        testCase(ranges = listOf(1..2, 60..70), limitRange = 1..100, target = 60..70)
    }

    private fun<T> propertyTest(
        ranges: List<IntRange>,
        limitRange: IntRange,
        property: ComplexRangeFragmentList<IntFragmentElement>.() -> T,
        expected: T
    ) {
        val list = fragmentList(ranges, limitRange)
        val actual = list.property()

        assertEquals(expected, actual)
    }

    private fun<T> propertyTestRepeat(
        ranges: List<IntRange>,
        limitRange: IntRange,
        property: ComplexRangeFragmentList<IntFragmentElement>.() -> T,
        expected: T
    ) {
        propertyTest(ranges, limitRange, property, expected)
        propertyTest(ranges, limitRange, property, expected)
    }

    private inline fun<reified E : Exception> propertyFailsTest(
        ranges: List<IntRange>,
        limitRange: IntRange,
        method: ComplexRangeFragmentList<IntFragmentElement>.() -> Unit
    ) {
        val list = fragmentList(ranges, limitRange)

        assertFailsWith<E> { list.method() }
    }

    private fun IntRangeFragment.toIntRange(): IntRange {
        return start.value..endInclusive.value
    }

    private fun fragmentList(ranges: List<IntRange>, limitRange: IntRange): ComplexRangeFragmentList<IntFragmentElement> {
        return BitIntComplexRange(limitRange.first, limitRange.last) {
            ranges.forEach { fragment(it) }
        }.fragments()
    }
}