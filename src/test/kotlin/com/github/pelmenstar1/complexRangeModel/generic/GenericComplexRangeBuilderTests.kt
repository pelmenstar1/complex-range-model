package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class GenericComplexRangeBuilderTests : BaseComplexRangeBuilderTests() {
    override fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
        return IntComplexRange(block)
    }
}