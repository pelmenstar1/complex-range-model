package com.github.pelmenstar1.complexRangeModel.bitsLong

import com.github.pelmenstar1.complexRangeModel.*

class BitLongIntComplexRangeBuilderTests : BaseComplexRangeBuilderTests() {
    override fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
        return BitLongIntComplexRange(limitStart = -1, block)
    }
}