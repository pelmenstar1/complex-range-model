package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*

class GenericComplexRangeTests : BaseComplexRangeTests() {
    override fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): ComplexRange<IntFragmentElement> {
        return IntComplexRange(block)
    }
}