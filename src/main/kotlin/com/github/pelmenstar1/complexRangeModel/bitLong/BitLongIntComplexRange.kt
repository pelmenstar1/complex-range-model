package com.github.pelmenstar1.complexRangeModel.bitLong

import com.github.pelmenstar1.complexRangeModel.*
import com.github.pelmenstar1.complexRangeModel.bits.endMask
import com.github.pelmenstar1.complexRangeModel.bits.startMask

internal class BitLongIntComplexRange(
    private val bits: Long,
    private val limitStart: Int
) : ComplexRange<IntFragmentElement> {
    private var fragments: FragmentsImpl? = null
    private var elements: ElementsImpl? = null

    override fun modify(block: ComplexRangeModify<IntFragmentElement>.() -> Unit): ComplexRange<IntFragmentElement> {
        val newBits = BitLongIntComplexRangeModify(bits, limitStart).also(block).bits

        return BitLongIntComplexRange(newBits, limitStart)
    }

    override fun fragments(): ComplexRangeFragmentList<IntFragmentElement> {
        var result = fragments
        if (result == null) {
            result = FragmentsImpl()
            fragments = result
        }
        return result
    }

    override fun elements(): Collection<IntFragmentElement> {
        var result = elements
        if (result == null) {
            result = ElementsImpl()
            elements = result
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is ComplexRange<*> -> fragments().fragmentIterator().contentEquals(other.fragments().fragmentIterator())
            else -> false
        }
    }

    override fun hashCode(): Int {
        var hash = 1
        forEachRange { bitStart, bitEnd ->
            val start = bitStart + limitStart
            val end = bitEnd + limitStart

            hash = hash * 31 + (start * 31 + end)
        }

        return hash
    }

    override fun toString(): String {
        return buildString {
            append("ComplexRange(")

            var isFirst = true
            forEachRange { bitStart, bitEnd ->
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

    private fun findNextSetBitIndex(startIndex: Int): Int {
        return findNextBitBase(startIndex, mapWord = { it })
    }

    private fun findNextUnsetBitIndex(startIndex: Int): Int {
        return findNextBitBase(startIndex, mapWord = Long::inv)
    }

    private inline fun findNextBitBase(startIndex: Int, mapWord: (Long) -> Long): Int {
        if (startIndex >= 64) {
            return -1
        }

        val bits = mapWord(bits) and startMask(startIndex)
        if (bits != 0L) {
            return bits.countTrailingZeroBits()
        }

        return -1
    }

    private fun findPreviousSetBitIndex(startIndex: Int): Int {
        return findPreviousBitBase(startIndex, mapWord = { it })
    }

    private fun findPreviousUnsetBitIndex(startIndex: Int): Int {
        return findPreviousBitBase(startIndex, mapWord = Long::inv)
    }

    private inline fun findPreviousBitBase(startIndex: Int, mapWord: (Long) -> Long): Int {
        if (startIndex <= 0) {
            return -1
        }

        val word = mapWord(bits) and endMask (startIndex)

        if (word != 0L) {
            return WORD_BIT_COUNT - 1 - word.countLeadingZeroBits()
        }

        return -1
    }

    private inline fun forEachRange(block: (start: Int, endInclusive: Int) -> Unit) {
        var start = 0

        while(true) {
            val rangeStart = findNextSetBitIndex(start)
            if (rangeStart < 0) {
                break
            }

            var unsetBitIndex = findNextUnsetBitIndex(rangeStart)
            if (unsetBitIndex < 0) {
                unsetBitIndex = WORD_BIT_COUNT
            }

            start = unsetBitIndex
            block(rangeStart, unsetBitIndex - 1)
        }
    }

    private inline fun forEachRangeReversed(block: (start: Int, endInclusive: Int) -> Unit) {
        var start = WORD_BIT_COUNT - 1

        while (true) {
            val setBitIndex = findPreviousSetBitIndex(start)
            if (setBitIndex < 0) {
                break
            }

            val unsetBitIndex = findPreviousUnsetBitIndex(setBitIndex)

            block(unsetBitIndex + 1, setBitIndex)
            start = unsetBitIndex - 1
        }
    }

    inner class FragmentsImpl : ComplexRangeFragmentList<IntFragmentElement> {
        private var _size = -1

        override val size: Int
            get() {
                var result = _size
                if (result < 0) {
                    result = computeSize()
                    _size = result
                }

                return result
            }

        private fun computeSize(): Int {
            var result = 0
            forEachRange { _, _ ->
                result++
            }
            return result
        }

        override fun isEmpty(): Boolean {
            return bits == 0L
        }

        override fun contains(element: RangeFragment<IntFragmentElement>): Boolean {
            return useFragment(element, default = false) { bitStart, bitEnd ->
                val mask = startMask(bitStart) and endMask(bitEnd)

                var wideMask = mask
                if (bitStart > 0) {
                    wideMask = wideMask or (1L shl (bitStart - 1))
                }
                if (bitEnd < WORD_BIT_COUNT - 1) {
                    wideMask = wideMask or (1L shl (bitEnd + 1))
                }

                bits and wideMask == mask
            }
        }

        override fun containsAll(elements: Collection<RangeFragment<IntFragmentElement>>): Boolean {
            return elements.all { it in this }
        }

        override fun includes(fragment: RangeFragment<IntFragmentElement>): Boolean {
            return useFragment(fragment, default = false) { bitStart, bitEnd ->
                val mask = startMask(bitStart) and endMask(bitEnd)

                bits and mask == mask
            }
        }

        override fun get(index: Int): RangeFragment<IntFragmentElement> {
            val s = _size
            if (index >= 0 && (s < 0 || index < s)) {
                var seqIndex = 0
                forEachRange { start, endInclusive ->
                    if(seqIndex == index){
                        return IntRangeFragment(start + limitStart, endInclusive + limitStart)
                    }
                    seqIndex++
                }
            }

            throw IndexOutOfBoundsException("index")
        }

        override fun indexOf(element: RangeFragment<IntFragmentElement>): Int {
            return useFragment(element, default = -1) { bitStart, bitEnd ->
                var index = 0
                forEachRange { start, endInclusive ->
                    if (bitStart == start && bitEnd == endInclusive) {
                        return index
                    }
                    index++
                }
                return -1
            }
        }

        override fun lastIndexOf(element: RangeFragment<IntFragmentElement>): Int {
            return useFragment(element, default = -1) { bitStart, bitEnd ->
                var index = 0
                forEachRangeReversed { start, endInclusive ->
                    if (bitStart == start && bitEnd == endInclusive) {
                        // Compute size only when we actually found a fragment
                        return size - index - 1
                    }
                    index++
                }
                return -1
            }
        }

        private inline fun<R> useFragment(fragment: RangeFragment<IntFragmentElement>, default: R, block: (bitStart: Int, bitEnd: Int) -> R): R {
            val start = fragment.start.value
            val endInclusive = fragment.endInclusive.value

            val bitStart = start - limitStart
            val bitEnd = endInclusive - limitStart

            if (bitStart >= 0 && bitEnd < WORD_BIT_COUNT) {
                return block(bitStart, bitEnd)
            }

            return default
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<RangeFragment<IntFragmentElement>> {
            if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
                throw IllegalArgumentException("Invalid range")
            }

            val newSize = toIndex - fromIndex
            if (newSize > size) {
                throw IndexOutOfBoundsException()
            }

            return SubFragmentList(this, fromIndex, toIndex)
        }

        override fun iterator(): Iterator<RangeFragment<IntFragmentElement>> {
            return listIterator()
        }

        override fun listIterator(): ListIterator<RangeFragment<IntFragmentElement>> {
            return fragmentIterator().toListIterator()
        }

        override fun listIterator(index: Int): ListIterator<RangeFragment<IntFragmentElement>> {
            if (index < 0) {
                throw IndexOutOfBoundsException("index")
            }

            return FragmentsIterator().also {
                it.skipFragments(index)
            }.toListIterator()
        }

        override fun fragmentIterator(): ComplexRangeFragmentListIterator<IntFragmentElement> {
            return FragmentsIterator()
        }
    }

    class SubFragmentList(
        private val wrappingList: FragmentsImpl,
        private val startIndex: Int,
        private val endIndex: Int
    ) : List<RangeFragment<IntFragmentElement>> {
        override val size: Int
            get() = endIndex - startIndex

        override fun isEmpty(): Boolean {
            return startIndex == endIndex
        }

        override fun get(index: Int): RangeFragment<IntFragmentElement> {
            if (index < 0 || index >= size) {
                throw IndexOutOfBoundsException()
            }

            return wrappingList[index - startIndex]
        }

        override fun indexOf(element: RangeFragment<IntFragmentElement>): Int {
            return indexOfBase { indexOf(element) }
        }

        override fun lastIndexOf(element: RangeFragment<IntFragmentElement>): Int {
            return indexOfBase { lastIndexOf(element) }
        }

        private fun indexOfBase(method: FragmentsImpl.() -> Int): Int {
            val index = wrappingList.method()

            if (index in startIndex..<endIndex) {
                return index - startIndex
            }

            return -1
        }

        override fun contains(element: RangeFragment<IntFragmentElement>): Boolean {
            return indexOf(element) >= 0
        }

        override fun containsAll(elements: Collection<RangeFragment<IntFragmentElement>>): Boolean {
            return elements.all { it in this }
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<RangeFragment<IntFragmentElement>> {
            if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
                throw IllegalArgumentException("Invalid range")
            }

            val newSize = toIndex - fromIndex
            if (newSize > size) {
                throw IndexOutOfBoundsException()
            }

            return SubFragmentList(wrappingList, startIndex + fromIndex, startIndex + toIndex)
        }

        override fun iterator(): Iterator<RangeFragment<IntFragmentElement>> {
            return listIterator()
        }

        override fun listIterator(): ListIterator<RangeFragment<IntFragmentElement>> {
            return listIterator(0)
        }

        override fun listIterator(index: Int): ListIterator<RangeFragment<IntFragmentElement>> {
            if (index < 0 || index > size) {
                throw IndexOutOfBoundsException("index")
            }

            return wrappingList.listIterator(startIndex + index).limitTo(size)
        }
    }

    inner class ElementsImpl : Collection<IntFragmentElement> {
        private var _size: Int = -1
        override val size: Int
            get() {
                var result = _size
                if (result < 0) {
                    result = bits.countOneBits()
                    _size = result
                }
                return result
            }

        override fun isEmpty(): Boolean {
            return bits == 0L
        }

        override fun contains(element: IntFragmentElement): Boolean {
            val bitPos = element.value - limitStart

            return bitPos in 0..<64 && (bits and (1L shl bitPos)) != 0L
        }

        override fun containsAll(elements: Collection<IntFragmentElement>): Boolean {
            var mask = 0L
            elements.forEach {
                val bitPos = it.value - limitStart
                if (bitPos !in 0..64) {
                    return false
                }

                mask = mask or (1L shl bitPos)
            }

            return bits and mask == mask
        }

        override fun iterator(): Iterator<IntFragmentElement> {
            return ElementsIterator()
        }
    }

    inner class FragmentsIterator : ComplexRangeFragmentListIterator<IntFragmentElement> {
        private var _current: RangeFragment<IntFragmentElement>? = null
        override val current: RangeFragment<IntFragmentElement>
            get() = _current ?: throw NoSuchElementException()

        private var lastFragmentStart = -2
        private var lastFragmentEnd = -1

        private var markedBitIndex = -1

        override fun moveNext(): Boolean {
            val setBitIndex = findNextSetBitIndex(lastFragmentEnd + 1)
            if (setBitIndex < 0) {
                return false
            }

            var unsetBitIndex = findNextUnsetBitIndex(setBitIndex)
            if (unsetBitIndex < 0) {
                unsetBitIndex = WORD_BIT_COUNT
            }

            setFragment(setBitIndex, unsetBitIndex - 1)

            return true
        }

        override fun movePrevious(): Boolean {
            val setBitIndex = findPreviousSetBitIndex(lastFragmentStart - 1)
            if (setBitIndex < 0) {
                return false
            }

            var unsetBitIndex = findPreviousUnsetBitIndex(setBitIndex)
            unsetBitIndex = maxOf(unsetBitIndex, -1)

            setFragment(unsetBitIndex + 1, setBitIndex)

            return true
        }

        override fun mark() {
            markedBitIndex = lastFragmentStart
        }

        override fun subRange(): ComplexRange<IntFragmentElement> {
            val markedBitIndex = markedBitIndex
            if (markedBitIndex < 0) {
                throw IllegalStateException("No marked element")
            }

            val newBits = bits and (startMask(markedBitIndex) and endMask(lastFragmentEnd))

            return BitLongIntComplexRange(newBits, limitStart)
        }

        private fun setFragment(bitStart: Int, bitEnd: Int) {
            lastFragmentStart = bitStart
            lastFragmentEnd = bitEnd

            _current = IntRangeFragment(bitStart + limitStart, bitEnd + limitStart)
        }

        fun skipFragments(n: Int) {
            var rem = n
            forEachRange { start, endInclusive ->
                if (rem == 0) {
                    lastFragmentStart = start
                    lastFragmentEnd = endInclusive
                }
                rem--
            }
        }
    }

    inner class ElementsIterator: Iterator<IntFragmentElement> {
        private var currentBitIndex = 0
        private var nextSetBitIndex = 0
        private var isNextSetBitIndexDirty = true

        private fun findNextSetBit(): Int {
            var result = nextSetBitIndex

            if (isNextSetBitIndexDirty) {
                result = findNextSetBitIndex(currentBitIndex)
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
        private const val WORD_SHIFT = 6
        private const val WORD_BIT_COUNT = 1 shl WORD_SHIFT
    }
}