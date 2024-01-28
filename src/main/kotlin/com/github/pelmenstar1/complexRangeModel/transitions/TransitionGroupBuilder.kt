package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.*

class TransitionGroupBuilder<T : FragmentElement<T>> {
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

    fun move(origin: RangeFragment<T>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Move(origin, destination))
    }

    fun split(origin: RangeFragment<T>, destinations: Array<RangeFragment<T>>) {
        ops.add(TransitionOperation.Split(origin, destinations))
    }

    fun join(origins: Array<RangeFragment<T>>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Join(origins, destination))
    }

    fun build(): TransitionGroup<T> = TransitionGroup.create(ops)
}

fun TransitionGroupBuilder<IntFragmentElement>.insert(range: IntRange) {
    insert(IntRangeFragment(range))
}

fun TransitionGroupBuilder<IntFragmentElement>.remove(range: IntRange) {
    remove(IntRangeFragment(range))
}

fun TransitionGroupBuilder<IntFragmentElement>.join(originRanges: Array<IntRange>, destRange: IntRange) {
    join(originRanges.mapToArray { IntRangeFragment(it) }, IntRangeFragment(destRange))
}

fun TransitionGroupBuilder<IntFragmentElement>.split(originRange: IntRange, destRanges: Array<IntRange>) {
    split(IntRangeFragment(originRange), destRanges.mapToArray { IntRangeFragment(it) })
}

fun TransitionGroupBuilder<IntFragmentElement>.transform(origin: IntRange, dest: IntRange){
    transform(IntRangeFragment(origin), IntRangeFragment(dest))
}

fun TransitionGroupBuilder<IntFragmentElement>.move(origin: IntRange, dest: IntRange){
    move(IntRangeFragment(origin), IntRangeFragment(dest))
}