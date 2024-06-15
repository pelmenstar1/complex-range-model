package com.github.pelmenstar1.complexRangeModel.bitLong

import com.github.pelmenstar1.complexRangeModel.*
import com.github.pelmenstar1.complexRangeModel.bits.*
import com.github.pelmenstar1.complexRangeModel.bits.AbstractBitElementsIterator
import com.github.pelmenstar1.complexRangeModel.bits.AbstractBitFragmentIterator
import com.github.pelmenstar1.complexRangeModel.bits.endMask
import com.github.pelmenstar1.complexRangeModel.bits.rangeMask
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

    inner class FragmentsImpl : AbstractBitFragmentList() {
        override fun computeSize(): Int {
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
                wideMask = wideMask or (1L shl maxOf(0, bitStart - 1))
                wideMask = wideMask or (1L shl minOf(WORD_BIT_COUNT - 1, bitEnd + 1))

                bits and wideMask == mask
            }
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

        override fun fragmentIterator(): ComplexRangeFragmentListIterator<IntFragmentElement> {
            return FragmentsIterator()
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
                if (bitPos !in 0..<64) {
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

    inner class FragmentsIterator : AbstractBitFragmentIterator(
        initialBitStart = 0,
        bitEndIndex = WORD_BIT_COUNT - 1,
        limitStart
    ) {
        override fun findNextSetBitIndex(start: Int): Int =
            this@BitLongIntComplexRange.findNextSetBitIndex(start)

        override fun findNextUnsetBitIndex(start: Int): Int  =
            this@BitLongIntComplexRange.findNextUnsetBitIndex(start)

        override fun findPreviousSetBitIndex(start: Int): Int =
            this@BitLongIntComplexRange.findPreviousSetBitIndex(start)

        override fun findPreviousUnsetBitIndex(start: Int): Int =
            this@BitLongIntComplexRange.findPreviousUnsetBitIndex(start)

        override fun createSubRange(startIndex: Int, endIndex: Int): ComplexRange<IntFragmentElement> {
            val newBits = bits and rangeMask(startIndex, endIndex)

            return BitLongIntComplexRange(newBits, limitStart)
        }
    }

    inner class ElementsIterator: AbstractBitElementsIterator(limitStart) {
        override fun findNextSetBitIndex(bitStart: Int): Int {
            return this@BitLongIntComplexRange.findNextSetBitIndex(bitStart)
        }
    }

    companion object {
        private const val WORD_SHIFT = 6
        private const val WORD_BIT_COUNT = 1 shl WORD_SHIFT
    }
}