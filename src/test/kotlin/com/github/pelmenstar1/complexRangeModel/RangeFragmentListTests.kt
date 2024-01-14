package com.github.pelmenstar1.complexRangeModel

import kotlin.test.*

class RangeFragmentListTests {
    private fun createList(ranges: Array<IntRange>): RangeFragmentList<Int> {
        return RangeFragmentList<Int>().apply {
            ranges.forEach { add(IntRangeFragment(it)) }
        }
    }

    @Test
    fun sizeTest() {
        fun testCase(ranges: Array<IntRange>) {
            val list = createList(ranges)
            val actual = list.size

            assertEquals(ranges.size, actual)
        }

        testCase(emptyArray())
        testCase(arrayOf(0..0))
        testCase(arrayOf(1..2, 3..4))
        testCase(arrayOf(1..2, 3..4, 5..6))
    }

    @Test
    fun isEmptyTest() {
        fun testCase(ranges: Array<IntRange>) {
            val list = createList(ranges)

            assertEquals(ranges.isEmpty(), list.isEmpty())
        }

        testCase(emptyArray())
        testCase(arrayOf(0..0))
    }

    @Test
    fun addTest() {
        fun testCase(ranges: Array<IntRange>) {
            val list = createList(ranges)

            validateList(list, ranges)
        }

        testCase(emptyArray())
        testCase(arrayOf(0..0))
        testCase(arrayOf(1..2, 3..4))
        testCase(arrayOf(1..2, 3..4, 5..6))
    }

    @Test
    fun getAtIndexTest() {
        fun testCase(ranges: Array<IntRange>) {
            val list = createList(ranges)

            for (i in ranges.indices) {
                val actual = toIntRange(list[i])
                val expected = ranges[i]

                assertEquals(expected, actual)
            }
        }

        testCase(arrayOf(0..0))
        testCase(arrayOf(1..2, 3..4))
        testCase(arrayOf(1..2, 3..4, 5..6))
    }

    @Test
    fun insertAfterNodeTest() {
        fun testCase(
            initialRanges: Array<IntRange>,
            insertAfterIndex: Int,
            newRange: IntRange,
            expectedRanges: Array<IntRange>
        ) {
            val list = createList(initialRanges)
            val node = list.getNode(insertAfterIndex)

            list.insertAfterNode(IntRangeFragment(newRange), node)

            validateList(list, expectedRanges)
        }

        testCase(
            initialRanges = arrayOf(0..1),
            insertAfterIndex = 0,
            newRange = 2..3,
            expectedRanges = arrayOf(0..1, 2..3)
        )

        testCase(
            initialRanges = arrayOf(0..1, 4..5),
            insertAfterIndex = 0,
            newRange = 2..3,
            expectedRanges = arrayOf(0..1, 2..3, 4..5)
        )

        testCase(
            initialRanges = arrayOf(0..1, 4..5),
            insertAfterIndex = 1,
            newRange = 2..3,
            expectedRanges = arrayOf(0..1, 4..5, 2..3)
        )
    }

    @Test
    fun insertBeforeHeadTest() {
        fun testCase(
            initialRanges: Array<IntRange>,
            newRange: IntRange,
            expectedRanges: Array<IntRange>
        ) {
            val list = createList(initialRanges)

            list.insertBeforeHead(IntRangeFragment(newRange))

            validateList(list, expectedRanges)
        }

        testCase(
            initialRanges = emptyArray(),
            newRange = 0..0,
            expectedRanges = arrayOf(0..0)
        )

        testCase(
            initialRanges = arrayOf(1..1),
            newRange = 0..0,
            expectedRanges = arrayOf(0..0, 1..1)
        )

        testCase(
            initialRanges = arrayOf(1..1, 2..2),
            newRange = 0..0,
            expectedRanges = arrayOf(0..0, 1..1, 2..2)
        )
    }

    @Test
    fun removeValueTest() {
        fun testCase(initialRanges: Array<IntRange>, valueToRemove: IntRange, expectedRanges: Array<IntRange>) {
            val list = createList(initialRanges)
            list.remove(IntRangeFragment(valueToRemove))

            validateList(list, expectedRanges)
        }

        testCase(
            initialRanges = arrayOf(0..0),
            valueToRemove = 0..0,
            expectedRanges = emptyArray()
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1),
            valueToRemove = 0..0,
            expectedRanges = arrayOf(1..1)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2),
            valueToRemove = 0..0,
            expectedRanges = arrayOf(1..1, 2..2)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2),
            valueToRemove = 1..1,
            expectedRanges = arrayOf(0..0, 2..2)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1),
            valueToRemove = 1..1,
            expectedRanges = arrayOf(0..0)
        )
    }

    @Test
    fun removeBetweenTest() {
        fun testCase(initialRanges: Array<IntRange>, startIndex: Int, endIndex: Int, expectedRanges: Array<IntRange>) {
            val list = createList(initialRanges)
            val removalStartNode = list.getNode(startIndex)
            val removalEndNode = list.getNode(endIndex)

            list.removeBetween(removalStartNode, removalEndNode)

            validateList(list, expectedRanges)
        }

        testCase(
            initialRanges = arrayOf(0..0),
            startIndex = 0,
            endIndex = 0,
            expectedRanges = emptyArray()
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1),
            startIndex = 0,
            endIndex = 1,
            expectedRanges = emptyArray()
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1),
            startIndex = 0,
            endIndex = 0,
            expectedRanges = arrayOf(1..1)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2),
            startIndex = 0,
            endIndex = 1,
            expectedRanges = arrayOf(2..2)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2),
            startIndex = 1,
            endIndex = 2,
            expectedRanges = arrayOf(0..0)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2),
            startIndex = 1,
            endIndex = 1,
            expectedRanges = arrayOf(0..0, 2..2)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2, 3..3),
            startIndex = 1,
            endIndex = 2,
            expectedRanges = arrayOf(0..0, 3..3)
        )
    }

    @Test
    fun replaceBetweenWithTest() {
        fun testCase(initialRanges: Array<IntRange>, replaceValue: IntRange, startIndex: Int, endIndex: Int, expectedRanges: Array<IntRange>) {
            val list = createList(initialRanges)
            val removalStartNode = list.getNode(startIndex)
            val removalEndNode = list.getNode(endIndex)

            list.replaceBetweenWith(IntRangeFragment(replaceValue), removalStartNode, removalEndNode)

            validateList(list, expectedRanges)
        }

        testCase(
            initialRanges = arrayOf(0..0),
            replaceValue = 1..1,
            startIndex = 0,
            endIndex = 0,
            expectedRanges = arrayOf(1..1)
        )

        testCase(
            initialRanges = arrayOf(0..0, 2..2),
            replaceValue = 1..1,
            startIndex = 0,
            endIndex = 0,
            expectedRanges = arrayOf(1..1, 2..2)
        )

        testCase(
            initialRanges = arrayOf(0..0, 2..2),
            replaceValue = 1..1,
            startIndex = 0,
            endIndex = 0,
            expectedRanges = arrayOf(1..1, 2..2)
        )

        testCase(
            initialRanges = arrayOf(0..0, 2..2),
            replaceValue = 1..1,
            startIndex = 1,
            endIndex = 1,
            expectedRanges = arrayOf(0..0, 1..1)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2, 3..3),
            replaceValue = 4..4,
            startIndex = 1,
            endIndex = 2,
            expectedRanges = arrayOf(0..0, 4..4, 3..3)
        )

        testCase(
            initialRanges = arrayOf(0..0, 1..1, 2..2, 3..3),
            replaceValue = 4..4,
            startIndex = 2,
            endIndex = 3,
            expectedRanges = arrayOf(0..0, 1..1, 4..4)
        )
    }

    @Test
    fun copyOfTest() {
        fun testCase(elements: Array<IntRange>) {
            val origin = createList(elements)
            val copy = origin.copyOf()

            var originCurrent = origin.head
            var copyCurrent = copy.head

            while (originCurrent != null) {
                assertNotNull(copyCurrent)
                assertNotSame(originCurrent, copyCurrent)

                originCurrent = originCurrent.next
                copyCurrent = copyCurrent.next
            }

            validateList(copy, elements)
        }

        testCase(emptyArray())
        testCase(arrayOf(1..1))
        testCase(arrayOf(1..1, 2..2))
        testCase(arrayOf(1..1, 2..2, 3..3))
    }

    @Test
    fun equalsTest() {
        fun testCase(firstElements: Array<IntRange>, secondElements: Array<IntRange>, expected: Boolean) {
            val firstList = createList(firstElements)
            val secondList = createList(secondElements)

            val actual = firstList == secondList
            assertEquals(expected, actual)
        }

        testCase(
            firstElements = emptyArray(),
            secondElements = emptyArray(),
            expected = true
        )

        testCase(
            firstElements = arrayOf(1..1),
            secondElements = emptyArray(),
            expected = false
        )

        testCase(
            firstElements = emptyArray(),
            secondElements = arrayOf(1..1),
            expected = false
        )

        testCase(
            firstElements = arrayOf(1..1),
            secondElements = arrayOf(1..1),
            expected = true
        )

        testCase(
            firstElements = arrayOf(1..1),
            secondElements = arrayOf(2..2),
            expected = false
        )

        testCase(
            firstElements = arrayOf(1..1, 2..2),
            secondElements = arrayOf(1..1, 2..2),
            expected = true
        )

        testCase(
            firstElements = arrayOf(1..1, 2..2),
            secondElements = arrayOf(1..1, 2..2, 3..3),
            expected = false
        )

        testCase(
            firstElements = arrayOf(1..1, 2..2, 3..3),
            secondElements = arrayOf(1..1, 2..2, 3..3),
            expected = true
        )
    }

    @Test
    fun toStringTest() {
        fun testCase(elements: Array<IntRange>, expected: String) {
            val list = createList(elements)
            val actual = list.toString()

            assertEquals(expected, actual)
        }

        testCase(emptyArray(), expected = "RangeFragmentList()")
        testCase(arrayOf(1..1), expected = "RangeFragmentList([1, 1])")
        testCase(arrayOf(1..1, 2..2), expected = "RangeFragmentList([1, 1], [2, 2])")
        testCase(arrayOf(1..1, 2..2, 3..3), expected = "RangeFragmentList([1, 1], [2, 2], [3, 3])")
    }

    private fun validateList(list: RangeFragmentList<Int>, expectedElements: Array<IntRange>) {
        val expectedSize = expectedElements.size
        val head = list.head
        val tail = list.tail

        if (expectedSize == 0) {
            assertNull(head)
            assertNull(tail)
            return
        } else if (expectedSize == 1) {
            assertNotNull(head)
            assertSame(head, tail)
            return
        }

        assertNotNull(head)
        assertNotNull(tail)
        assertNull(head.previous)
        assertNull(tail.next)

        var index = 0

        var previous: RangeFragmentList.Node<Int>? = null
        var current: RangeFragmentList.Node<Int>? = head

        while (current != null) {
            val expectedValue = IntRangeFragment(expectedElements[index])
            val actualValue = current.value

            assertSame(previous, current.previous)
            assertEquals(expectedValue, actualValue)

            index++
            previous = current
            current = current.next
        }

        assertSame(tail, previous)
        assertEquals(expectedSize, index)
    }

    private fun RangeFragmentList<Int>.getIntRanges(): Array<IntRange> {
        return map { toIntRange(it) }.toTypedArray()
    }

    private fun toIntRange(fragment: RangeFragment<Int>): IntRange {
        return fragment.start..fragment.endInclusive
    }
}
