package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment
import com.github.pelmenstar1.complexRangeModel.RawLinkedList

abstract class GenericComplexRangeBaseBuilder<T : FragmentElement<T>> {
    protected val fragments: RawLinkedList<RangeFragment<T>>

    protected constructor() {
        fragments = RawLinkedList()
    }

    protected constructor(fragments: RawLinkedList<RangeFragment<T>>) {
        this.fragments = fragments
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
        startNode: RawLinkedList.Node<RangeFragment<T>>
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
        affectedNode: RawLinkedList.Node<RangeFragment<T>>
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
        affectedStartNode: RawLinkedList.Node<RangeFragment<T>>,
        affectedEndNode: RawLinkedList.Node<RangeFragment<T>>
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
        splitNode: RawLinkedList.Node<RangeFragment<T>>,
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

    private fun findFirstOverlapFragmentNode(fragment: RangeFragment<T>): RawLinkedList.Node<RangeFragment<T>>? {
        return fragments.findFirstNode { fragment.overlapsWith(it) }
    }

    private fun findLastOverlapFragmentNode(fragment: RangeFragment<T>): RawLinkedList.Node<RangeFragment<T>>? {
        return fragments.findLastNode { fragment.overlapsWith(it) }
    }
}