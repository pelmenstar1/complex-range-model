package com.github.pelmenstar1.complexRangeModel

abstract class ComplexRangeBaseBuilder<T : Comparable<T>> {
    protected var fragments: Array<RangeFragment<T>>
    private val support: RangeFragmentSupport<T>

    protected constructor(support: RangeFragmentSupport<T>) {
        this.support = support
        fragments = emptyArray()
    }

    protected constructor(support: RangeFragmentSupport<T>, fragments: Array<RangeFragment<T>>) {
        this.support = support
        this.fragments = fragments
    }

    protected fun includeFragment(fragment: RangeFragment<T>) {
        if (fragments.isEmpty()) {
            fragments = arrayOf(fragment)
            return
        }

        for (i in fragments.indices) {
            val thisFragment = fragments[i]

            if (thisFragment == fragment) {
                // No need for creating new array if we're adding the same fragment to the array
                return
            }

            val unitedFragment = thisFragment.uniteWith(fragment)
            if (unitedFragment != null) {
                includeFragmentWithUniting(unitedFragment, i)
                return
            }
        }

        // Given fragment cannot be united with any of current fragments.
        // Then find its index (for the ranges to be sorted) and insert it
        includeFragmentWithoutUniting(fragment)
    }

    private fun includeFragmentWithUniting(fragmentUnitedWithStart: RangeFragment<T>, startIndex: Int) {
        var totalUnitedFragment = fragmentUnitedWithStart

        // If the loop below is not ended by finding the range that cannot be united with currentOverlapFragment,
        // then all ranges, starting from startIndex, should be united
        // The index is exclusive.
        var endIndex = fragments.size

        for (i in (startIndex + 1) until fragments.size) {
            val currentFragment = fragments[i]

            val unitedFragment = totalUnitedFragment.uniteWith(currentFragment)
            if (unitedFragment != null) {
                totalUnitedFragment = unitedFragment
            } else {
                endIndex = i
                break
            }
        }

        replaceFragmentsWith(totalUnitedFragment, startIndex, endIndex)
    }

    private fun replaceFragmentsWith(fragment: RangeFragment<T>, startIndex: Int, endIndex: Int) {
        val newSize = fragments.size - (endIndex - startIndex - 1)
        val newFragments = arrayOfFragmentsUnsafe(newSize)

        // Copy elements before startIndex
        fragments.copyInto(newFragments, destinationOffset = 0, endIndex = startIndex)
        newFragments[startIndex] = fragment

        // Copy elements from fragments after endIndex to newFragments after startIndex
        fragments.copyInto(
            newFragments,
            destinationOffset = startIndex + 1,
            startIndex = endIndex,
            endIndex = fragments.size
        )

        // All values are not null
        fragments = newFragments
    }

    private fun includeFragmentWithoutUniting(fragment: RangeFragment<T>) {
        val insertIndex = findNewFragmentInsertIndex(fragment)

        insertFragment(insertIndex, fragment)
    }

    private fun findNewFragmentInsertIndex(fragment: RangeFragment<T>): Int {
        // It assumes that given fragment doesn't overlap with any of current fragments
        for (i in fragments.indices) {
            if (fragment.start < fragments[i].start) {
                return i - 1
            }
        }

        return fragments.size - 1
    }

    protected fun excludeFragment(fragment: RangeFragment<T>) {
        val affectedRangeStart = indexOfFirstOverlapFragment(fragment)
        val affectedRangeEnd = indexOfLastOverlapFragment(fragment)

        if (affectedRangeStart == -1) {
            return
        }

        if (affectedRangeStart == affectedRangeEnd) {
            excludeFragmentWithOneAffected(fragment, affectedRangeStart)
        } else {
            excludeFragmentWithRangeAffected(fragment, affectedRangeStart, affectedRangeEnd)
        }
    }

    private fun excludeFragmentWithOneAffected(excludeFragment: RangeFragment<T>, index: Int) {
        val affectedFragment = fragments[index]

        if (excludeFragment.containsCompletely(affectedFragment)) {
            // Simply remove the whole range
            removeFragmentRange(index, index + 1)
        } else if (affectedFragment.containsExclusive(excludeFragment)) {
            splitFragmentWithExcludingOtherFragment(index, excludeFragment)
        } else if (affectedFragment.leftContains(excludeFragment)) {
            // Something like:
            // affectedFragment:    [] [] | [] []
            // excludeFragment:  [] [] [] |
            // result:                    | [] []
            // So we need to remove the part where these fragments intersect
            fragments[index] = affectedFragment.withStart(support.next(excludeFragment.endInclusive))
        } else {
            // affectedFragment is not fragmentToRemove, affectedFragment doesn't contain the fragmentToRemove (exclusively)
            // and the affectedFragment doesn't left-contains the fragmentToRemove,
            // then the affectedFragment right-contains the fragmentToRemove
            //
            // Something like:
            // affectedFragment: [] [] | [] []
            // excludeFragment:        | [] [] []
            // result:           [] [] |
            fragments[index] = affectedFragment.withEnd(support.previous(excludeFragment.start))
        }
    }

    private fun excludeFragmentWithRangeAffected(
        excludeFragment: RangeFragment<T>,
        affectedStartIndex: Int,
        affectedEndIndexInclusive: Int
    ) {
        val affectedStartFrag = fragments[affectedStartIndex]
        val affectedEndFrag = fragments[affectedEndIndexInclusive]

        val removalStartIndex: Int

        // Exclusive index
        val removalEndIndex: Int

        if (excludeFragment.start <= affectedStartFrag.start) {
            // Something like:
            // affectedStartFrag:    [] [] []
            // excludeFragment:   [] [] [] [] ...
            // So we need to remove the whole affectedStartFrag
            removalStartIndex = affectedStartIndex
        } else {
            // Something like:
            // affectedStartFrag: [] [] | [] [] []
            // excludeFragment:         | [] [] [] ...
            // result:            [] []
            // So we need to narrow the affectedStartFrag and remove starting from the next fragment
            removalStartIndex = affectedStartIndex + 1
            fragments[affectedStartIndex] = affectedStartFrag.withEndExclusive(excludeFragment.start)
        }

        if (excludeFragment.endInclusive >= affectedEndFrag.endInclusive) {
            // Something like:
            // affectedEndFrag:     [] [] []
            // excludeFragment: ... [] [] [] []
            // So we need to remove whole affectedEndFrag
            removalEndIndex = affectedEndIndexInclusive + 1
        } else {
            // Something like:
            // affectedEndFrag:     [] [] | [] []
            // excludeFragment: ... [] [] |
            // result:                      [] []
            // So we need no narrow affectedEndFrag and remove ending with the previous fragment
            removalEndIndex = affectedEndIndexInclusive
            fragments[affectedEndIndexInclusive] = affectedEndFrag.withStart(excludeFragment.endExclusive)
        }

        if (removalStartIndex < removalEndIndex) {
            removeFragmentRange(removalStartIndex, removalEndIndex)
        }
    }

    private fun splitFragmentWithExcludingOtherFragment(splitIndex: Int, excludeFragment: RangeFragment<T>) {
        // Range at splitIndex: [] [] [] [] [] []
        // excludeFragment:           [] []
        // result:              [] []       [] []
        val splitFragment = fragments[splitIndex]

        // Change range at splitIndex to the left part of the split.
        fragments[splitIndex] = splitFragment.withEndExclusive(excludeFragment.start)

        // Insert the right part of the split after splitIndex
        insertFragment(splitIndex, splitFragment.withStart(excludeFragment.endExclusive))
    }

    private fun insertFragment(index: Int, fragment: RangeFragment<T>) {
        val newFragments = arrayOfFragmentsUnsafe(fragments.size + 1)

        // Copy elements before insertAfterIndex
        fragments.copyInto(newFragments, destinationOffset = 0, endIndex = index + 1)

        newFragments[index + 1] = fragment

        // Copy elements after insertAfterIndex
        fragments.copyInto(
            newFragments,
            destinationOffset = index + 2,
            startIndex = index + 1
        )

        // All values are not null
        fragments = newFragments
    }

    private fun removeFragmentRange(startIndex: Int, endIndex: Int) {
        val newSize = fragments.size - (endIndex - startIndex)
        val newFragments = arrayOfFragmentsUnsafe(newSize)
        fragments.copyInto(newFragments, endIndex = startIndex)
        fragments.copyInto(newFragments, destinationOffset = startIndex, startIndex = endIndex)

        fragments = newFragments
    }

    private fun indexOfFirstOverlapFragment(fragment: RangeFragment<T>): Int {
        return fragments.indexOfFirst { fragment.overlapsWith(it) }
    }

    private fun indexOfLastOverlapFragment(fragment: RangeFragment<T>): Int {
        return fragments.indexOfLast { fragment.overlapsWith(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun arrayOfFragmentsUnsafe(size: Int): Array<RangeFragment<T>> {
        return arrayOfNulls<RangeFragment<T>>(size) as Array<RangeFragment<T>>
    }
}