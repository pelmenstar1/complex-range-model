package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange

interface ComplexRange<T> : Collection<RangeFragment<T>> {
    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T>

    fun twoWayIterator(): TwoWayIterator<RangeFragment<T>>

    companion object {
        fun<T> empty(): ComplexRange<T> = GenericComplexRange.empty()
    }
}




