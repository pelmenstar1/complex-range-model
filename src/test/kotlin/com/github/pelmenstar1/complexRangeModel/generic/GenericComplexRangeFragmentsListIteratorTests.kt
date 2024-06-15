package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*

class GenericComplexRangeFragmentsListIteratorTests : BaseComplexRangeFragmentListIteratorTests() {
    override fun createIterator(data: List<IntRangeFragment>): ComplexRangeFragmentListIterator<IntFragmentElement> {
        return ComplexRange(data).fragments().fragmentIterator()
    }
}