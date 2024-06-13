package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*
import com.github.pelmenstar1.complexRangeModel.IntRangeFragment

class BitIntComplexRangeBuilder(private val limitStart: Int, private val limitEnd: Int) : ComplexRangeBuilder<IntFragmentElement> {
    private val bitSet = FixedBitSet(limitEnd - limitStart + 1)

    override fun fragment(value: IntRangeFragment) {
        val fragStart = value.start.value
        val fragEnd = value.endInclusive.value

        ensureValidRange(fragStart, fragEnd)

        bitSet.set(fragStart - limitStart, fragEnd - limitStart)
    }

    override fun value(v: IntFragmentElement) {
        val value = v.value
        ensureValidValue(value)

        bitSet.set(value - limitStart)
    }

    override fun values(vs: Array<out IntFragmentElement>) {
        var i = 0

        addValuesInternal(hasNext = { i < vs.size }, next = { vs[i++].value })
    }

    override fun values(vs: Iterable<IntFragmentElement>) {
        val iter = vs.iterator()

        addValuesInternal(iter::hasNext, next = { iter.next().value })
    }

    fun valuesDirect(vs: IntArray) {
        var i = 0

        addValuesInternal(hasNext = { i < vs.size }, next = { vs[i++] })
    }

    fun valuesDirect(vs: Iterable<Int>) {
        val iter = vs.iterator()

        addValuesInternal(iter::hasNext, iter::next)
    }

    private inline fun addValuesInternal(hasNext: () -> Boolean, next: () -> Int) {
        var currentWordMask = 0L
        var currentWordIndex = -1

        while(hasNext()) {
            val value = next()
            ensureValidValue(value)

            val bitIndex = value - limitStart
            val wordIndex = FixedBitSet.getWordIndex(bitIndex)
            val bitMask = 1L shl bitIndex

            if (currentWordIndex == wordIndex) {
                currentWordMask = currentWordMask or bitMask
            } else {
                if (currentWordIndex >= 0) {
                    bitSet.setMask(currentWordIndex, currentWordMask)
                }

                currentWordMask = bitMask
                currentWordIndex = wordIndex
            }
        }

        if (currentWordIndex >= 0) {
            bitSet.setMask(currentWordIndex, currentWordMask)
        }
    }

    private fun isValidRange(start: Int, endInclusive: Int): Boolean {
        return start >= limitStart && endInclusive <= limitEnd
    }

    private fun isValidValue(value: Int): Boolean {
        return value in limitStart..limitEnd
    }

    private fun ensureValidRange(start: Int, endInclusive: Int) {
        if (!isValidRange(start, endInclusive)) {
            throw IllegalArgumentException("Specified range is out of the limiting range")
        }
    }

    private fun ensureValidValue(value: Int) {
        if (!isValidValue(value)) {
            throw IllegalArgumentException("Specified value is out of the limiting range")
        }
    }

    fun build(): IntComplexRange {
       return BitIntComplexRange(bitSet, limitStart, limitEnd)
    }
}