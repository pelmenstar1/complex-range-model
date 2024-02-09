package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.*

/**
 * Represents a builder for [TransitionGroup] object.
 */
class TransitionGroupBuilder<T : FragmentElement<T>> {
    private val ops = ArrayList<TransitionOperation<T>>()

    /**
     * Adds [TransitionOperation.Insert] operation to the group.
     *
     * @param fragment a fragment to insert
     */
    fun insert(fragment: RangeFragment<T>) {
        ops.add(TransitionOperation.Insert(fragment))
    }

    /**
     * Adds [TransitionOperation.Remove] operation to the group.
     *
     * @param fragment a fragment to remove
     */
    fun remove(fragment: RangeFragment<T>) {
        ops.add(TransitionOperation.Remove(fragment))
    }

    /**
     * Adds [TransitionOperation.Transform] operation to the group.
     *
     * [origin] and [destination] should not be equal, and they should overlap.
     *
     * @param origin a fragment to begin transition with
     * @param destination a fragment to end transition with
     */
    fun transform(origin: RangeFragment<T>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Transform(origin, destination))
    }

    /**
     * Adds [TransitionOperation.Move] operation to the group.
     *
     * [origin] and [destination] should not be equal, and they should have the same [RangeFragment.elementCount] value.
     *
     * @param origin a fragment to begin transition with
     * @param destination a fragment to end transition with
     */
    fun move(origin: RangeFragment<T>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Move(origin, destination))
    }

    /**
     * Adds [TransitionOperation.Split] operation to the group.
     *
     * @param origin a fragment to split
     * @param destinations an array of fragments to end the split operation with.
     * The array is transformed into [ComplexRange],
     * so that the resulting fragments are mutually non-overlapping and ordered,
     * even through the initial array of fragments may not have these properties.
     */
    fun split(origin: RangeFragment<T>, destinations: Array<out RangeFragment<T>>) {
        split(origin, ComplexRange(destinations))
    }

    /**
     * Adds [TransitionOperation.Split] operation to the group.
     *
     * @param origin a fragment to split
     * @param destination a sequence of fragments to end up with after the transition. This sequence should be completely in the [origin] fragment
     */
    fun split(origin: RangeFragment<T>, destination: ComplexRange<T>) {
        ops.add(TransitionOperation.Split(origin, destination))
    }

    /**
     * Adds [TransitionOperation.Join] operation to the group.
     *
     * @param origin an initial sequence of fragments to join
     * @param destination a final fragment after joining fragments of [origin]
     */
    fun join(origin: ComplexRange<T>, destination: RangeFragment<T>) {
        ops.add(TransitionOperation.Join(origin, destination))
    }

    /**
     * Adds [TransitionOperation.Join] operation to the group.
     *
     * @param origins an array of fragments to join.
     * The array is transformed to [ComplexRange],
     * so that the resulting fragments are mutually non-overlapping and ordered,
     * even through the initial array of fragments may not have these properties.
     */
    fun join(origins: Array<out RangeFragment<T>>, destination: RangeFragment<T>) {
        join(ComplexRange(origins), destination)
    }

    /**
     * Returns a new instance of [TransitionGroup].
     */
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