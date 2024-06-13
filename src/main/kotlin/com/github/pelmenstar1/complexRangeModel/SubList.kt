package com.github.pelmenstar1.complexRangeModel


internal class SubList<T>(
    private val wrappingList: List<T>,
    private val startIndex: Int,
    private val endIndex: Int
) : List<T> {
    override val size: Int
        get() = endIndex - startIndex

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun get(index: Int): T {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException()
        }

        return wrappingList[index - startIndex]
    }

    override fun indexOf(element: T): Int {
        return mapIndexOfResult(wrappingList.indexOf(element))
    }

    override fun lastIndexOf(element: T): Int {
        return mapIndexOfResult(wrappingList.lastIndexOf(element))
    }

    private fun mapIndexOfResult(index: Int): Int {
        if (index in startIndex..<endIndex) {
            return index - startIndex
        }

        return -1
    }

    override fun contains(element: T): Boolean {
        return indexOf(element) >= 0
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { it in this }
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
            throw IllegalArgumentException("Invalid range")
        }

        val newSize = toIndex - fromIndex
        if (newSize > size) {
            throw IndexOutOfBoundsException()
        }

        return SubList(wrappingList, startIndex + fromIndex, startIndex + toIndex)
    }

    override fun iterator(): Iterator<T> {
        return listIterator()
    }

    override fun listIterator(): ListIterator<T> {
        return wrappingList.listIterator(startIndex).limitTo(size)
    }

    override fun listIterator(index: Int): ListIterator<T> {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException("index")
        }

        return wrappingList.listIterator(startIndex + index).limitTo(size)
    }
}