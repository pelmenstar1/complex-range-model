package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment
import com.github.pelmenstar1.complexRangeModel.RawLinkedList

private typealias FragmentNode<T> = RawLinkedList.Node<RangeFragment<T>>

internal abstract class GenericComplexRangeBaseBuilder<T : FragmentElement<T>> {
    protected val fragments: RawLinkedList<RangeFragment<T>>

    protected constructor() {
        fragments = RawLinkedList()
    }

    protected constructor(fragments: RawLinkedList<RangeFragment<T>>) {
        this.fragments = fragments
    }

    protected fun includeValue(newValue: T): FragmentNode<T> {
        fragments.forEachNode { node ->
            tryIncludeValueFromNodeForward(newValue, node)?.let {
                return it
            }
        }

        return fragments.addAndReturnNode(RangeFragment(newValue, newValue))
    }

    private fun tryIncludeValueFromNodeBase(newValue: T, node: FragmentNode<T>): FragmentNode<T>? {
        val frag = node.value

        return when {
            // Bail out. The complex range already contains this value.
            frag.contains(newValue) -> node
            frag.isAdjacentLeft(newValue) -> uniteWithValueRight(newValue, node)
            frag.isAdjacentRight(newValue) -> uniteWithValueLeft(newValue, node)
            else -> null
        }
    }

    private fun tryIncludeValueFromNodeForward(newValue: T, node: FragmentNode<T>): FragmentNode<T>? {
        var result = tryIncludeValueFromNodeBase(newValue, node)
        if (result == null && newValue < node.value.start) {
            result = fragments.insertBeforeNode(RangeFragment(newValue, newValue), node)
        }

        return result
    }

    private fun tryIncludeValueFromNodeBackward(newValue: T, node: FragmentNode<T>): FragmentNode<T>? {
        var result = tryIncludeValueFromNodeBase(newValue, node)
        if (result == null && newValue > node.value.endInclusive) {
            result = fragments.insertAfterNode(RangeFragment(newValue, newValue), node)
        }

        return result
    }

    private fun includeValueWithAnchor(newValue: T, anchorNode: FragmentNode<T>): FragmentNode<T> {
        val anchorFrag = anchorNode.value

        tryIncludeValueFromNodeBase(newValue, anchorNode)?.let {
            return it
        }

        // If we can't include a newValue using anchorNode, then newValue is either before or after anchorFrag
        // (but not inside)

        if (newValue < anchorFrag.start) {
            fragments.forEachNodeReversedStartingWith(anchorNode.previous) { node ->
                tryIncludeValueFromNodeBackward(newValue, node)?.let {
                    return it
                }
            }

            return fragments.insertFirst(RangeFragment(newValue, newValue))
        } else {
            // Then newValue is after anchorFrag.

            fragments.forEachNodeStartingWith(anchorNode.next) { node ->
                tryIncludeValueFromNodeForward(newValue, node)?.let {
                    return it
                }
            }

            return fragments.addAndReturnNode(RangeFragment(newValue, newValue))
        }
    }

    private fun uniteWithValueRight(newValue: T, currentNode: FragmentNode<T>): FragmentNode<T> {
        val frag = currentNode.value
        val nextNode = currentNode.next

        if (nextNode != null) {
            val nextFrag = nextNode.value

            if (nextFrag.isAdjacentRight(newValue)) {
                val newFrag = RangeFragment(frag.start, nextFrag.endInclusive)
                fragments.replaceBetweenWith(newFrag, currentNode, nextNode)

                return currentNode
            }
        }

        currentNode.value = RangeFragment(frag.start, newValue)
        return currentNode
    }

    private fun uniteWithValueLeft(newValue: T, currentNode: FragmentNode<T>): FragmentNode<T> {
        val frag = currentNode.value
        val prevNode = currentNode.previous

        if (prevNode != null) {
            val prevFrag = prevNode.value

            if (prevFrag.isAdjacentLeft(newValue)) {
                val newFrag = RangeFragment(prevFrag.start, frag.endInclusive)
                fragments.replaceBetweenWith(newFrag, prevNode, currentNode)

                return prevNode
            }
        }

        currentNode.value = RangeFragment(newValue, frag.endInclusive)
        return currentNode
    }

    protected fun includeValues(values: Array<out T>) {
        var i = 0

        includeValues(hasNext = { i < values.size }, next = { values[i++] })
    }

    protected fun includeValues(values: Iterable<T>) {
        val iter = values.iterator()

        includeValues(iter::hasNext, iter::next)
    }

    private inline fun includeValues(
        hasNext: () -> Boolean,
        next: () -> T
    ) {
        if (!hasNext()) {
            return
        }

        var lastNode = includeValue(next())

        while (hasNext()) {
            lastNode = includeValueWithAnchor(next(), lastNode)
        }
    }

    protected fun includeFragment(fragment: RangeFragment<T>) {
        fragments.forEachNode { node ->
            val thisFragment = node.value

            if (thisFragment.containsCompletely(fragment)) {
                // Bail out. No sense in adding the fragment that is already in the 'fragments' list.
                // (Even if thisFragment isn't equal to 'fragment', thisFragment contains all the values from 'fragment')
                return
            }

            if (thisFragment.canUniteWith(fragment)) {
                val minElement = minOf(thisFragment.start, fragment.start)
                val maxElement = maxOf(thisFragment.endInclusive, fragment.endInclusive)

                includeFragmentWithUniting(minElement, maxElement, node)
                return
            }

            if (fragment.isBefore(thisFragment)) {
                fragments.insertBeforeNode(fragment, node)
                return
            }
        }

        // If we can't unite given fragment with any other fragments and can't find a position to insert the fragment,
        // then add it to the end - it's greater than other fragments
        fragments.add(fragment)
    }

    private fun includeFragmentWithUniting(
        minElement: T,
        initialMaxElement: T,
        startNode: FragmentNode<T>
    ) {
        var maxElement = initialMaxElement

        // Last node cannot be null because the list is not empty.
        var endNode = fragments.tail

        fragments.forEachNodeStartingWith(startNode.next) { node ->
            val currentFragment = node.value

            if (currentFragment.overlapsWith(minElement, maxElement) || currentFragment.isAdjacentRight(maxElement)) {
                // currentFragment.endInclusive is always greater than current value of maxElement
                maxElement = currentFragment.endInclusive
            } else {
                // node.previous cannot be null, because we start iterating from startNode.next
                // (which is not null if we're here)
                endNode = node.previous

                // Bail out from the loop - if currentFragment cannot be united with fragment [minElement, maxElement],
                // then no next fragments can.
                return@forEachNodeStartingWith
            }
        }

        val unitedFragment = RangeFragment(minElement, maxElement)
        fragments.replaceBetweenWith(unitedFragment, startNode, endNode!!)
    }

    protected fun excludeFragment(fragment: RangeFragment<T>) {
        val affectedStartNode = findFirstOverlapFragmentNode(fragment)
        val affectedEndNode = findLastOverlapFragmentNode(fragment)

        if (affectedStartNode == null || affectedEndNode == null) {
            return
        }

        if (affectedStartNode === affectedEndNode) {
            excludeFragmentWithOneAffected(fragment, affectedStartNode)
        } else {
            excludeFragmentWithRangeAffected(fragment, affectedStartNode, affectedEndNode)
        }
    }

    private fun excludeFragmentWithOneAffected(
        excludeFragment: RangeFragment<T>,
        affectedNode: FragmentNode<T>
    ) {
        val affectedFragment = affectedNode.value

        if (excludeFragment.containsCompletely(affectedFragment)) {
            // Simply remove the whole fragment
            fragments.removeNode(affectedNode)
        } else if (affectedFragment.containsExclusive(excludeFragment)) {
            splitFragmentWithExcludingOtherFragment(affectedNode, excludeFragment)
        } else if (affectedFragment.leftContains(excludeFragment)) {
            // affectedFragment:    [] [] | [] []
            // excludeFragment:  [] [] [] |
            // result:                    | [] []
            // So we need to remove the part where these fragments intersect
            //
            // Existence of excludeFragment.endExclusive is implied by existence of affectedFragment.endInclusive,
            // that is greater than excludeFragment.endExclusive
            affectedNode.value = affectedFragment.withStart(excludeFragment.endExclusive)
        } else {
            // affectedFragment is not fragmentToRemove, affectedFragment doesn't contain the fragmentToRemove (exclusively)
            // and the affectedFragment doesn't left-contains the fragmentToRemove,
            // then the affectedFragment right-contains the fragmentToRemove
            //
            // affectedFragment: [] [] | [] []
            // excludeFragment:        | [] [] []
            // result:           [] [] |
            //
            // Existence of excludeFragment.start.previous() is implied by existence of affectedFragment.start
            // that is lesser than excludeFragment.start
            affectedNode.value = affectedFragment.withEndExclusive(excludeFragment.start)
        }
    }

    private fun excludeFragmentWithRangeAffected(
        excludeFragment: RangeFragment<T>,
        affectedStartNode: FragmentNode<T>,
        affectedEndNode: FragmentNode<T>
    ) {
        val affectedStartFrag = affectedStartNode.value
        val affectedEndFrag = affectedEndNode.value

        val removalStartNode: RawLinkedList.Node<RangeFragment<T>>?
        val removalEndNode: RawLinkedList.Node<RangeFragment<T>>?

        if (excludeFragment.isBefore(affectedStartFrag)) {
            // affectedStartFrag:    [] [] []
            // excludeFragment:   [] [] [] [] ...
            // So we need to remove the whole affectedStartFrag
            removalStartNode = affectedStartNode
        } else {
            // affectedStartFrag: [] [] | [] [] []
            // excludeFragment:         | [] [] [] ...
            // result:            [] []
            // So we need to narrow the affectedStartFrag and remove starting from the next fragment
            removalStartNode = affectedStartNode.next

            // Existence of excludeFragment.start.previous() is implied by existence of affectedStartFrag.start
            // that is lesser than excludeFragment.start
            affectedStartNode.value = affectedStartFrag.withEndExclusive(excludeFragment.start)
        }

        if (excludeFragment.isAfter(affectedEndFrag)) {
            // Something like:
            // affectedEndFrag:     [] [] []
            // excludeFragment: ... [] [] [] []
            // So we need to remove whole affectedEndFrag
            removalEndNode = affectedEndNode
        } else {
            // Something like:
            // affectedEndFrag:     [] [] | [] []
            // excludeFragment: ... [] [] |
            // result:                      [] []
            // So we need no narrow affectedEndFrag and remove ending with the previous fragment
            removalEndNode = affectedEndNode.previous

            // Existence of excludeFragment.endExclusive is implied by existence of affectedEndFrag.endInclusive
            // that is greater than excludeFragment.endInclusive
            affectedEndNode.value = affectedEndFrag.withStart(excludeFragment.endExclusive)
        }

        if (removalStartNode != null && removalEndNode != null) {
            if (removalStartNode.value.isBefore(removalEndNode.value)) {
                fragments.removeBetween(removalStartNode, removalEndNode)
            }
        }
    }

    private fun splitFragmentWithExcludingOtherFragment(
        splitNode: FragmentNode<T>,
        excludeFragment: RangeFragment<T>
    ) {
        // Range in splitNode: [] [] [] [] [] []
        // excludeFragment:           [] []
        // result:              [] []       [] []
        // Existence of excludeFragment.start.previous() and excludeFragment.endExclusive is implied by existence of
        // start and endInclusive of the range in splitNode.

        val splitFragment = splitNode.value

        // Change range at splitIndex to the left part of the split.
        splitNode.value = splitFragment.withEndExclusive(excludeFragment.start)

        // Insert the right part of the split after splitIndex
        fragments.insertAfterNode(splitFragment.withStart(excludeFragment.endExclusive), splitNode)
    }

    private fun findFirstOverlapFragmentNode(fragment: RangeFragment<T>): FragmentNode<T>? {
        return fragments.findFirstNode { fragment.overlapsWith(it) }
    }

    private fun findLastOverlapFragmentNode(fragment: RangeFragment<T>): FragmentNode<T>? {
        return fragments.findLastNode { fragment.overlapsWith(it) }
    }
}