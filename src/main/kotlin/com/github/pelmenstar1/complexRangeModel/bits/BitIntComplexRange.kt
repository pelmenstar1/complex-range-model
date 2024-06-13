package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*

internal class BitIntComplexRange(
    private val bitSet: FixedBitSet,
    private val limitStart: Int,
    private val limitEnd: Int,
) : ComplexRange<IntFragmentElement> {
    private var fragments: FragmentListImpl? = null
    private var elements: ElementsCollection? = null

    override fun modify(block: ComplexRangeModify<IntFragmentElement>.() -> Unit): ComplexRange<IntFragmentElement> {
        val bits = bitSet.copyOf()
        BitIntComplexRangeModify(bits, limitStart, limitEnd).block()

        return BitIntComplexRange(bits, limitStart, limitEnd)
    }

    override fun fragments(): FragmentListImpl {
        var result = fragments
        if (result == null) {
            result = FragmentListImpl()
            fragments = result
        }
        return result
    }

    override fun elements(): Collection<IntFragmentElement> {
        var result = elements
        if (result == null) {
            result = ElementsCollection()
            elements = result
        }
        return result
    }

    private fun isValidRange(start: Int, endInclusive: Int): Boolean {
        return start in limitStart..endInclusive && endInclusive <= limitEnd
    }

    private fun isValidValue(value: Int): Boolean {
        return value in limitStart..limitEnd
    }

    private fun bitPosition(value: Int): Int {
        return value - limitStart
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is ComplexRange<*> -> fragments().fragmentIterator().contentEquals(other.fragments().fragmentIterator())
            else -> false
        }
    }

    override fun hashCode(): Int {
        val ls = limitStart
        var hash = 1

        bitSet.forEachRange { start, endInclusive ->
            hash = hash * 31 + ((start + ls) * 31 + (endInclusive + ls))
        }

        return hash
    }

    override fun toString(): String {
        return buildString {
            append("ComplexRange(")

            var isFirst = true
            bitSet.forEachRange { bitStart, bitEnd ->
                if (!isFirst) {
                    append(", ")
                }

                isFirst = false
                append('[')
                append(bitStart + limitStart)
                append(", ")
                append(bitEnd + limitStart)
                append(']')
            }

            append(')')
        }
    }

    inner class FragmentListImpl : ComplexRangeFragmentList<IntFragmentElement> {
        private var _size = NOT_COMPUTED

        override val size: Int
            get() {
                var s = _size
                if (s == NOT_COMPUTED) {
                    s = bitSet.countRanges()
                    _size = s
                }

                return s
            }

        fun getRawSize(): Int = _size

        override fun isEmpty(): Boolean {
            return _size == 0 || bitSet.isEmpty()
        }

        override fun get(index: Int): RangeFragment<IntFragmentElement> {
            val size = _size

            if (index >= 0 && (size == NOT_COMPUTED || index < size)) {
                var seqIndex = 0
                bitSet.forEachRange { start, endInclusive ->
                    if (seqIndex == index) {
                        return IntRangeFragment(limitStart + start, limitStart + endInclusive)
                    }

                    seqIndex++
                }
            }

            throw IndexOutOfBoundsException()
        }

        override fun last(): RangeFragment<IntFragmentElement> {
            val setBitIndex = bitSet.lastSetBitIndex()
            if (setBitIndex < 0) {
                throw IndexOutOfBoundsException()
            }

            var unsetBitIndex = bitSet.findPreviousUnsetBitIndex(setBitIndex)
            unsetBitIndex = maxOf(unsetBitIndex, -1)

            return IntRangeFragment(unsetBitIndex + limitStart + 1, setBitIndex + limitStart)
        }

        override fun contains(element: RangeFragment<IntFragmentElement>): Boolean {
            return useIfValidFragmentOr(element, false) { start, end ->
                bitSet.containsRange(start, end)
            }
        }

        override fun containsAll(elements: Collection<RangeFragment<IntFragmentElement>>): Boolean {
            return elements.all { it in this }
        }

        override fun includes(fragment: RangeFragment<IntFragmentElement>): Boolean {
            return useIfValidFragmentOr(fragment, false) { start, end ->
                bitSet.includesRange(start, end)
            }
        }

        override fun indexOf(element: RangeFragment<IntFragmentElement>): Int {
            return useIfValidFragmentOr(element, -1) { startBitIndex, endBitIndex ->
                var index = 0

                bitSet.forEachRange { bitStart, bitEnd ->
                    if (startBitIndex == bitStart && endBitIndex == bitEnd) {
                        return index
                    }
                    index++
                }

                return -1
            }
        }

        override fun lastIndexOf(element: RangeFragment<IntFragmentElement>): Int {
            return useIfValidFragmentOr(element, -1) { startBitIndex, endBitIndex ->
                var index = 0

                bitSet.forEachRangeReversed { bitStart, bitEnd ->
                    if (startBitIndex == bitStart && endBitIndex == bitEnd) {
                        // Compute (in some cases) size only in the case we actually found a fragment
                        return size - index - 1
                    }
                    index++
                }

                return -1
            }
        }

        private inline fun<T> useIfValidFragmentOr(
            fragment: RangeFragment<IntFragmentElement>,
            defaultValue: T,
            block: (Int, Int) -> T
        ): T {
            val startIndex = fragment.start.value
            val endIndex = fragment.endInclusive.value

            if (isValidRange(startIndex, endIndex)) {
                val startBitIndex = bitPosition(startIndex)
                val endBitIndex = bitPosition(endIndex)

                return block(startBitIndex, endBitIndex)
            }

            return defaultValue
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<RangeFragment<IntFragmentElement>> {
            if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
                throw IllegalArgumentException("Invalid range")
            }

            val newSize = toIndex - fromIndex
            if (newSize > size) {
                throw IndexOutOfBoundsException()
            }

            return SubList(this, fromIndex, toIndex)
        }

        override fun fragmentIterator(): ComplexRangeFragmentListIterator<IntFragmentElement> {
            return FragmentIterator()
        }

        override fun iterator(): Iterator<RangeFragment<IntFragmentElement>> {
            return listIterator()
        }

        override fun listIterator(): ListIterator<RangeFragment<IntFragmentElement>> {
            return fragmentIterator().toListIterator()
        }

        override fun listIterator(index: Int): ListIterator<RangeFragment<IntFragmentElement>> {
            // Force to compute real size
            if (index < 0 || index >= size) {
                throw IndexOutOfBoundsException()
            }

            return FragmentIterator().also {
                it.skipFragments(index)
            }.toListIterator()
        }
    }

    inner class FragmentIterator : AbstractBitFragmentIterator(
        initialBitStart = bitSet.bitStartIndex,
        bitEndIndex = bitSet.bitEndIndex,
        limitStart
    ) {
        override fun findNextSetBitIndex(start: Int): Int =
            bitSet.findNextSetBitIndex(start)

        override fun findNextUnsetBitIndex(start: Int): Int =
            bitSet.findNextUnsetBitIndex(start)

        override fun findPreviousSetBitIndex(start: Int): Int =
            bitSet.findPreviousSetBitIndex(start)

        override fun findPreviousUnsetBitIndex(start: Int): Int =
            bitSet.findPreviousUnsetBitIndex(start)

        override fun createSubRange(startIndex: Int, endIndex: Int): ComplexRange<IntFragmentElement> {
            val newBits = bitSet.select(startIndex, endIndex)

            return BitIntComplexRange(newBits, limitStart, limitEnd)
        }
    }

    inner class ElementsCollection : Collection<IntFragmentElement> {
        private var _size = NOT_COMPUTED

        override val size: Int
            get() {
                var result = _size
                if (result == NOT_COMPUTED) {
                    result = bitSet.countSetBits()
                    _size = result
                }

                return result
            }

        override fun isEmpty(): Boolean {
            return _size == 0 || bitSet.isEmpty()
        }

        override fun contains(element: IntFragmentElement): Boolean {
            val value = element.value

            return isValidValue(value) && bitSet[value - limitStart]
        }

        override fun containsAll(elements: Collection<IntFragmentElement>): Boolean {
            return elements.all { it in this }
        }

        override fun iterator(): Iterator<IntFragmentElement> = ElementsIterator()
    }

    inner class ElementsIterator : Iterator<IntFragmentElement> {
        private var currentBitIndex = 0
        private var nextSetBitIndex = 0
        private var isNextSetBitIndexDirty = true

        private fun findNextSetBit(): Int {
            var result = nextSetBitIndex

            if (isNextSetBitIndexDirty) {
                result = bitSet.findNextSetBitIndex(currentBitIndex)
                nextSetBitIndex = result
                isNextSetBitIndexDirty = false
            }

            return result
        }

        override fun hasNext(): Boolean {
            return findNextSetBit() >= 0
        }

        override fun next(): IntFragmentElement {
            val nextIndex = findNextSetBit()
            if (nextIndex < 0) {
                throw NoSuchElementException()
            }

            currentBitIndex = nextIndex + 1
            isNextSetBitIndexDirty = true

            return IntFragmentElement(nextIndex)
        }
    }

    companion object {
        private const val NOT_COMPUTED = -2
    }
}