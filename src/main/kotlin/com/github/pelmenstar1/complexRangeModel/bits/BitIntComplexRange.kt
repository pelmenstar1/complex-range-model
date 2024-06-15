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

    inner class FragmentListImpl : AbstractBitFragmentList() {
        override fun computeSize(): Int {
            return bitSet.countRanges()
        }

        override fun isEmpty(): Boolean {
            return _size == 0 || bitSet.isEmpty()
        }

        override fun get(index: Int): RangeFragment<IntFragmentElement> {
            val size = _size

            if (index >= 0 && (size < 0 || index < size)) {
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

        override fun fragmentIterator(): ComplexRangeFragmentListIterator<IntFragmentElement> {
            return FragmentIterator()
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
        private var _size = -1

        override val size: Int
            get() {
                var result = _size
                if (result < 0) {
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

    inner class ElementsIterator : AbstractBitElementsIterator(limitStart, currentBitIndex = bitSet.bitStartIndex) {
        override fun findNextSetBitIndex(bitStart: Int): Int {
            return bitSet.findNextSetBitIndex(bitStart)
        }
    }
}