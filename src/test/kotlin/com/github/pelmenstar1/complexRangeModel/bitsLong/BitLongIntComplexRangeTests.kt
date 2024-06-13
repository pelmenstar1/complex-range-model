package com.github.pelmenstar1.complexRangeModel.bitsLong

import com.github.pelmenstar1.complexRangeModel.*

class BitLongIntComplexRangeTests : BaseComplexRangeTests() {
    override fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
        return BitLongIntComplexRange(limitStart = -3, block)
    }
}