package com.github.pelmenstar1.complexRangeModel

import kotlin.test.*

class ArraySetTests {
    @Test
    fun clearTest() {
        val set = createArraySet(arrayOf(0, 1, 2))

        set.clear()
        assertTrue(set.isEmpty())
        assertEquals(0, set.size)
    }

    @Test
    fun getThrowsOnOutOfBounds() {
        val set = ArraySet<Int>(capacity = 4)

        assertFails { set[2] }
    }

    @Test
    fun constructorWithElementsTest() {
        fun testCase(elements: Array<Int>) {
            val set = ArraySet(elements.toList())

            elements.forEach {
                assertTrue(set.contains(it))
            }
        }

        testCase(elements = arrayOf(0))
        testCase(elements = arrayOf(0, 1))
        testCase(elements = arrayOf(0, 1, 2))
    }

    @Test
    fun addTest() {
        fun testCase(elements: Array<Int>, preSize: Int = 0) {
            val arraySet = createArraySet(elements, preSize)

            val arraySetElements = arraySet.toTypedArray()
            val setElements = arraySetElements.toHashSet()
            val expectedElements = elements.toHashSet()

            assertEquals(expectedElements, setElements)
        }

        testCase(elements = arrayOf(0))
        testCase(elements = arrayOf(0, 0))
        testCase(elements = arrayOf(0), preSize = 4)
        testCase(elements = arrayOf(0, -1, 5))
        testCase(elements = arrayOf(0, 7, 5, 10))
        testCase(elements = arrayOf(0, 7, 5, 10, 5))
        testCase(elements = arrayOf(0, 7, 5, 10), preSize = 4)
    }

    @Test
    fun containsTest() {
        fun testCase(elements: Array<Int>, needle: Int, expected: Boolean, preSize: Int = 0) {
            val arraySet = createArraySet(elements, preSize)
            val actual = arraySet.contains(needle)

            assertEquals(expected, actual)
        }

        testCase(elements = arrayOf(0), needle = 0, expected = true)
        testCase(elements = arrayOf(0), needle = 1, expected = false)
        testCase(elements = arrayOf(0), needle = 0, expected = true, preSize = 4)
        testCase(elements = arrayOf(0, -1, 5), needle = -1, expected = true)
        testCase(elements = arrayOf(0, -1, 5), needle = 2, expected = false)
        testCase(elements = arrayOf(0, 7, 5, 10), needle = 10, expected = true)
        testCase(elements = arrayOf(0, 7, 5, 10), needle = 10, expected = true, preSize = 4)
    }

    @Test
    fun getIndexOfTest() {
        fun testCase(elements: Array<Int>, preSize: Int = 0) {
            val set = createArraySet(elements, preSize)

            elements.forEachIndexed { i, element ->
                val actualIndex = set.indexOf(element)
                val actualElement = set[actualIndex]

                assertEquals(actualElement, element, "element")
            }
        }

        testCase(elements = arrayOf(0))
        testCase(elements = arrayOf(0), preSize = 4)
        testCase(elements = arrayOf(0, -1, 5))
        testCase(elements = arrayOf(0, 7, 5, 10))
        testCase(elements = arrayOf(0, 7, 5, 10), preSize = 4)
    }

    @Test
    fun equalsTest() {
        fun testCase(first: Array<Int>, second: Array<Int>, expected: Boolean) {
            val firstSet = createArraySet(first)
            val secondSet = createArraySet(second)
            val actual = firstSet == secondSet

            assertEquals(expected, actual)
        }

        testCase(emptyArray(), emptyArray(), expected = true)
        testCase(arrayOf(1), arrayOf(1), expected = true)
        testCase(arrayOf(1), arrayOf(2), expected = false)
        testCase(arrayOf(1), arrayOf(1, 2), expected = false)
        testCase(arrayOf(1, 2), arrayOf(1), expected = false)
        testCase(arrayOf(1, 3, 2), arrayOf(1, 2, 3), expected = true)
    }

    @Test
    fun removeTest() {
        fun testCase(initial: Array<Int>, elementToRemove: Int) {
            val set = createArraySet(initial)
            set.remove(elementToRemove)

            val actualElements = set.toTypedArray()
            assertFalse(actualElements.contains(elementToRemove))
            assertFalse(set.contains(elementToRemove))
        }

        testCase(arrayOf(0), elementToRemove = 0)
        testCase(arrayOf(0), elementToRemove = 1)
        testCase(arrayOf(0, 1, 2), elementToRemove = 1)
        testCase(arrayOf(0, 1, 2), elementToRemove = 2)
        testCase(arrayOf(0, 1, 2), elementToRemove = 0)
    }

    private fun createArraySet(elements: Array<Int>, preSize: Int = 0): ArraySet<Int> {
        return ArraySet<Int>(preSize).apply {
            elements.forEach { add(it) }
        }
    }
}