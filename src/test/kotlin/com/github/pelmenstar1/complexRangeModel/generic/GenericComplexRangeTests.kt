package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.BaseComplexRangeTests
import com.github.pelmenstar1.complexRangeModel.ComplexRange
import com.github.pelmenstar1.complexRangeModel.ComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.IntComplexRange

class GenericComplexRangeTests : BaseComplexRangeTests() {
    override fun createComplexRange(block: ComplexRangeBuilder<Int>.() -> Unit): ComplexRange<Int> {
        return IntComplexRange(block)
    }
}