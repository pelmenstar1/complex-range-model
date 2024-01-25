package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.*

class ComplexRangeTransitionManager<T>(
    private val fragmentFactory: RangeFragmentFactory<T>,
    private val proxPredicate: FragmentProximityPredicate<T> = FragmentProximityPredicate.uniting()
) {
    fun createTransition(origin: ComplexRange<T>, dest: ComplexRange<T>): ComplexRangeTransition<T> {
        val groups = ArrayList<TransitionGroup<T>>()
        val originIter = origin.twoWayIterator()
        val destIter = dest.twoWayIterator()

        while(true) {
            val originHasNext = originIter.hasNext()
            val destHasNext = destIter.hasNext()

            if (!originHasNext || !destHasNext) {
                if (originHasNext) {
                    // Then destHasNext is false
                    addRemoveAllTransition(originIter, groups)
                } else if (destHasNext) {
                    addInsertAllTransition(destIter, groups)
                }

                break
            }

            originIter.mark()
            destIter.mark()

            val originFrag = originIter.next()
            val destFrag = destIter.next()

            if (originFrag != destFrag) {
                if (proxPredicate.test(originFrag, destFrag)) {
                    consumeElementsForTransformGroup(originIter, destIter)

                    val originGroupIter = originIter.subIterator()
                    val destGroupIter = destIter.subIterator()

                    groups.add(createTransformGroup(originGroupIter, destGroupIter))
                } else {
                    groups.add(TransitionGroup.create(TransitionOperation.Remove(originFrag)))
                    groups.add(TransitionGroup.create(TransitionOperation.Insert(destFrag)))
                }
            }
        }

        return ComplexRangeTransition(groups)
    }

    private fun addInsertAllTransition(iter: TwoWayIterator<RangeFragment<T>>, groups: ArrayList<TransitionGroup<T>>) {
        addOperationAllTransition(iter, groups) { TransitionOperation.Insert(it) }
    }

    private fun addRemoveAllTransition(iter: TwoWayIterator<RangeFragment<T>>, groups: ArrayList<TransitionGroup<T>>) {
        addOperationAllTransition(iter, groups) { TransitionOperation.Remove(it) }
    }

    private inline fun addOperationAllTransition(
        iter: TwoWayIterator<RangeFragment<T>>,
        groups: ArrayList<TransitionGroup<T>>,
        createOp: (RangeFragment<T>) -> TransitionOperation<T>
    ) {
        while (iter.hasNext()) {
            val fragment = iter.next()
            val op = createOp(fragment)

            groups.add(TransitionGroup.create(op))
        }
    }

    private fun consumeElementsForTransformGroup(
        originIter: TwoWayIterator<RangeFragment<T>>,
        destIter: TwoWayIterator<RangeFragment<T>>
    ) {
        while (true) {
            consumeUnitingElements(originIter.peek(), destIter)

            val currentDestFrag = destIter.peek()
            consumeUnitingElements(currentDestFrag, originIter)

            val currentOriginFrag = originIter.peek()

            if (!proxPredicate.test(currentDestFrag, currentOriginFrag)) {
                break
            }

            if (!originIter.hasNext() && !destIter.hasNext()) {
                break
            }
        }
    }

    private fun consumeUnitingElements(
        anchorFrag: RangeFragment<T>,
        iter: TwoWayIterator<RangeFragment<T>>
    ) {
        while (iter.hasNext()) {
            val frag = iter.next()
            if (!proxPredicate.test(frag, anchorFrag)) {
                iter.previous()
                break
            }
        }
    }

    private fun createTransformGroup(
        originIter: TwoWayIterator<RangeFragment<T>>,
        destIter: TwoWayIterator<RangeFragment<T>>,
    ): TransitionGroup<T> {
        val ops = ArrayList<TransitionOperation<T>>(3)

        val originSize = originIter.size
        val destSize = destIter.size

        if (originSize == 1) {
            val originFrag = originIter.next()

            if (destSize == 1) {
                // Ops:
                // - Transform

                val destFrag = destIter.next()
                ops.add(TransitionOperation.Transform(originFrag, destFrag))
            } else {
                // Ops:
                // - Transform
                // - Split

                val destFrags = destIter.toTypedArray(destSize)

                val minFrag = destFrags[0]
                val maxFrag = destFrags[destSize - 1]
                val destTransformFrag = fragmentFactory.create(minFrag.start, maxFrag.endInclusive)

                if (originFrag != destTransformFrag) {
                    ops.add(TransitionOperation.Transform(originFrag, destTransformFrag))
                }

                ops.add(TransitionOperation.Split(destTransformFrag, destFrags))
            }
        } else {
            // originSize > 1
            val originFrags = originIter.toTypedArray(originSize)

            val minOriginFrag = originFrags[0]
            val maxOriginFrag = originFrags[originSize - 1]
            val originTransformFrag = fragmentFactory.create(minOriginFrag.start, maxOriginFrag.endInclusive)

            if (destSize == 1) {
                val destFrag = destIter.next()

                // Ops:
                // - Join
                // - Transform? (if joined fragment is not destination fragment)
                ops.add(TransitionOperation.Join(originFrags, originTransformFrag))

                if (destFrag != originTransformFrag) {
                    ops.add(TransitionOperation.Transform(originTransformFrag, destFrag))
                }
            } else {
                // Ops:
                // - Join
                // - Transform
                // - Split

                val destFrags = destIter.toTypedArray(destSize)

                val minDestFrag = destFrags[0]
                val maxDestFrag = destFrags[destSize - 1]

                val destTransformFrag = fragmentFactory.create(minDestFrag.start, maxDestFrag.endInclusive)

                ops.add(TransitionOperation.Join(originFrags, originTransformFrag))

                if (originTransformFrag != destTransformFrag) {
                    ops.add(TransitionOperation.Transform(originTransformFrag, destTransformFrag))
                }

                ops.add(TransitionOperation.Split(destTransformFrag, destFrags))
            }
        }

        return TransitionGroup.create(ops)
    }
}