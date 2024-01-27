package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange

interface ComplexRange<T : FragmentElement<T>> : Collection<RangeFragment<T>> {
    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T>

    fun twoWayIterator(): TwoWayIterator<RangeFragment<T>>

    companion object {
        fun <T : FragmentElement<T>> empty(): ComplexRange<T> = GenericComplexRange.empty()
    }
}




