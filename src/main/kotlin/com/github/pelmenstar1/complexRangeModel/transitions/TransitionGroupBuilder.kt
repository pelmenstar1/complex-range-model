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

    fun split(origin: RangeFragment<T>, destinations: Array<out RangeFragment<T>>) {
        split(origin, ComplexRange(destinations))
    }

    fun split(origin: RangeFragment<T>, destination: ComplexRange<T>) {
        ops.add(TransitionOperation.Split(origin, destination))
    }

    fun join(origin: ComplexRange<T>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Join(origin, destination))
    }

    fun join(origins: Array<out RangeFragment<T>>, destination: RangeFragment<T>) {
        join(ComplexRange(origins), destination)
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
    join(IntComplexRange(originRanges), IntRangeFragment(destRange))
}

fun TransitionGroupBuilder<IntFragmentElement>.split(originRange: IntRange, destRanges: Array<IntRange>) {
    split(IntRangeFragment(originRange), IntComplexRange(destRanges))
}

fun TransitionGroupBuilder<IntFragmentElement>.transform(origin: IntRange, dest: IntRange) {
    transform(IntRangeFragment(origin), IntRangeFragment(dest))
}

fun TransitionGroupBuilder<IntFragmentElement>.move(origin: IntRange, dest: IntRange) {
    move(IntRangeFragment(origin), IntRangeFragment(dest))
}