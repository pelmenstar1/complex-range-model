package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.IntRangeFragment
import com.github.pelmenstar1.complexRangeModel.RangeFragment
import com.github.pelmenstar1.complexRangeModel.mapToArray

class TransitionGroupBuilder<T> {
    private val ops = ArrayList<TransitionOperation<T>>()

    fun insert(fragment: RangeFragment<T>) {
        ops.add(TransitionOperation.Insert(fragment))
    }

    fun remove(fragment: RangeFragment<T>) {
        ops.add(TransitionOperation.Remove(fragment))
    }

    fun transform(origin: RangeFragment<T>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Transform(origin, destination))
    }

    fun split(origin: RangeFragment<T>, destinations: Array<RangeFragment<T>>) {
        ops.add(TransitionOperation.Split(origin, destinations))
    }

    fun join(origins: Array<RangeFragment<T>>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Join(origins, destination))
    }

    fun build(): TransitionGroup<T> = TransitionGroup.create(ops)
}

fun TransitionGroupBuilder<Int>.insert(range: IntRange) {
    insert(IntRangeFragment(range))
}

fun TransitionGroupBuilder<Int>.remove(range: IntRange) {
    remove(IntRangeFragment(range))
}

fun TransitionGroupBuilder<Int>.join(originRanges: Array<IntRange>, destRange: IntRange) {
    join(originRanges.mapToArray { IntRangeFragment(it) }, IntRangeFragment(destRange))
}

fun TransitionGroupBuilder<Int>.split(originRange: IntRange, destRanges: Array<IntRange>) {
    split(IntRangeFragment(originRange), destRanges.mapToArray { IntRangeFragment(it) })
}

fun TransitionGroupBuilder<Int>.transform(origin: IntRange, dest: IntRange){
    transform(IntRangeFragment(origin), IntRangeFragment(dest))
}