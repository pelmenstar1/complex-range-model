package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class GenericComplexRangeTests : BaseComplexRangeTests() {
    override fun createRange(block: ComplexRangeBuilder<Int>.() -> Unit): ComplexRange<Int> {
        return IntComplexRange(block)
    }
}