package com.github.pelmenstar1.complexRangeModel.bitLong

import com.github.pelmenstar1.complexRangeModel.*
import com.github.pelmenstar1.complexRangeModel.bits.rangeMask

class BitLongIntComplexRangeBuilder(private val limitStart: Int) : ComplexRangeBuilder<IntFragmentElement> {
    private var bits = 0L

    override fun fragment(value: IntRangeFragment) {
        val bitStart = value.start.value - limitStart
        val bitEnd = value.endInclusive.value - limitStart

        if (!(bitStart >= 0 && bitEnd < 64)) {
            throw IllegalArgumentException("Specified range is out of the limiting range")
        }

        bits = bits or rangeMask(bitStart, bitEnd)
    }

    override fun fragments(fs: Iterable<RangeFragment<IntFragmentElement>>) {
        val iter = fs.iterator()

        fragmentsInternal(hasNext = iter::hasNext, next = iter::next)
    }

    override fun fragments(fs: Array<out RangeFragment<IntFragmentElement>>) {
        var index = 0

        fragmentsInternal(hasNext = { index < fs.size }, next = { fs[index++] })
    }

    private inline fun fragmentsInternal(hasNext: () -> Boolean, next: () -> IntRangeFragment) {
        var mask = 0L

        while(hasNext()) {
            val fragment = next()
            val bitStart = fragment.start.value - limitStart
            val bitEnd = fragment.endInclusive.value - limitStart

            ensureValidBitRange(bitStart, bitEnd)

            mask = mask or rangeMask(bitStart, bitEnd)
        }

        bits = bits or mask
    }

    override fun value(v: IntFragmentElement) {
        val value = v.value
        ensureValidBitIndex(value)

        bits = bits or (1L shl (value - limitStart))
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
        var mask = 0L

        while(hasNext()) {
            val bitIndex = next() - limitStart
            ensureValidBitIndex(bitIndex)

            mask = mask or (1L shl bitIndex)
        }

        bits = bits or mask
    }

    private fun ensureValidBitRange(start: Int, endInclusive: Int) {
        if (!(start >= 0 && endInclusive < 64)) {
            throw IllegalArgumentException("Specified range is out of the limiting range")
        }
    }

    private fun ensureValidBitIndex(bitIndex: Int) {
        if (bitIndex !in 0..<64) {
            throw IllegalArgumentException("Specified value is out of the limiting range")
        }
    }

    fun build(): IntComplexRange {
        return BitLongIntComplexRange(bits, limitStart)
    }
}