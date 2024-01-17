package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.BaseComplexRangeTests
import com.github.pelmenstar1.complexRangeModel.BitIntComplexRange
import com.github.pelmenstar1.complexRangeModel.ComplexRange
import com.github.pelmenstar1.complexRangeModel.ComplexRangeBuilder

class BitArrayComplexRangeTests : BaseComplexRangeTests() {
    override fun createRange(block: ComplexRangeBuilder<Int>.() -> Unit): ComplexRange<Int> {
        return BitIntComplexRange(limitStart = -100, limitEnd = 1000, block)
    }
}