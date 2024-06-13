package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*

class BitComplexRangeTests : BaseComplexRangeTests() {
    override fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
        return BitIntComplexRange(-10, 100, block)
    }
}