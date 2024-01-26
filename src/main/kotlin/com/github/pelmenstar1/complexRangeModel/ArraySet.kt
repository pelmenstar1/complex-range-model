package com.github.pelmenstar1.complexRangeModel


class ArraySet<T> : MutableSet<T> {
    private var elements: Array<Any?>
    private var hashes: IntArray
    private var _size: Int = 0

    override val size: Int
        get() = _size

    constructor() {
        elements = emptyArray()
        hashes = IntArray(0)
    }

    constructor(capacity: Int) {
        elements = arrayOfNulls(capacity)
        hashes = IntArray(capacity)
    }

    constructor(col: Collection<T>) {
        val capacity = col.size

        elements = arrayOfNulls(capacity)
        hashes = IntArray(capacity)

        addAll(col)
    }

    override fun isEmpty(): Boolean = _size == 0

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int): T {
        if (index >= _size) {
            throw IndexOutOfBoundsException()
        }

        return elements[index] as T
    }

    fun indexOf(element: T): Int {
        val h = element.hashCode()

        return indexOfHash(element, h)
    }

    private fun indexOfHash(key: T, hash: Int): Int {
        // Important fast case: if nothing is in here, nothing to look for.
        if (_size == 0) {
            return 0.inv()
        }

        val index = hashes.binarySearch(hash, toIndex = _size)

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index
        }

        // If the key at the returned index matches, that's what we want.
        if (key == elements[index]) {
            return index
        }

        // Search for a matching key after the index.
        var end = index + 1

        while (end < size && hashes[end] == hash) {
            if (key == elements[end]) {
                return end
            }

            end++
        }

        // Search for a matching key before the index.
        var i = index - 1
        while (i >= 0 && hashes[i] == hash) {
            if (key == elements[i]) {
                return i
            }

            i--
        }

        return end.inv()
    }

    override fun contains(element: T): Boolean {
        return indexOf(element) >= 0
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun add(element: T): Boolean {
        val hash = element.hashCode()
        var index = indexOfHash(element, hash)
        val oSize = _size

        if (index >= 0) {
            return false
        }

        index = index.inv()

        if (oSize >= elements.size) {
            val n = maxOf(4, oSize * 2)

            val newHashes = IntArray(n)
            val newElements = arrayOfNulls<Any>(n)

            hashes.copyInto(newHashes)
            elements.copyInto(newElements)

            hashes = newHashes
            elements = newElements
        }

        if (index < oSize) {
            System.arraycopy(hashes, index, hashes, index + 1, oSize - index);
            System.arraycopy(elements, index, elements, index + 1, oSize - index);
        }

        elements[index] = element
        hashes[index] = hash
        _size = oSize + 1

        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var changed = false

        elements.forEach {
            changed = changed or add(it)
        }

        return changed
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }

    fun removeAt(index: Int) {
        val oSize = _size
        val nSize = oSize - 1

        if (index < 0 || index >= oSize) {
            throw IndexOutOfBoundsException()
        }

        if (index < nSize) {
            System.arraycopy(hashes, index + 1, hashes, index, nSize - index);
            System.arraycopy(elements, index + 1, elements, index, nSize - index);
        }
        elements[nSize] = null;

        _size = nSize
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var changed = false

        elements.forEach {
            changed = changed or add(it)
        }

        return changed
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var changed = false

        elements.forEach {
            if (!contains(it)) {
                changed = true
                remove(it)
            }
        }

        return changed
    }

    override fun clear() {
        elements.fill(null, toIndex = _size)
        _size = 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other is Set<*>) {
            val s = _size

            if (s != other.size) {
                return false
            }

            for (i in 0 until s) {
                if (!other.contains(elements[i])) {
                    return false
                }
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        val hashes = hashes

        for (i in 0 until _size) {
            result = result * 31 + hashes[i]
        }

        return result
    }

    override fun iterator(): MutableIterator<T> = IteratorImpl()

    private inner class IteratorImpl : MutableIterator<T> {
        private var index = 0

        override fun hasNext(): Boolean {
            return index < _size
        }

        @Suppress("UNCHECKED_CAST")
        override fun next(): T {
            return elements[index++] as T
        }

        override fun remove() {
            removeAt(index - 1)
        }

    }
}