package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange

interface ComplexRange<T : FragmentElement<T>> {
    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T>

    fun fragments(): List<RangeFragment<T>>
    fun elements(): List<T>

    companion object {
        fun <T : FragmentElement<T>> empty(): ComplexRange<T> = GenericComplexRange.empty()
    }
}




