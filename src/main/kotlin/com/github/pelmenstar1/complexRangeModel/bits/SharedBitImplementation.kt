package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*

internal abstract class AbstractBitElementsIterator(
    private val limitStart: Int,
    private var currentBitIndex: Int = 0,
) : Iterator<IntFragmentElement> {
    private var nextSetBitIndex = 0
    private var isNextSetBitIndexDirty = true

    protected abstract fun findNextSetBitIndex(bitStart: Int): Int

    private fun findNextSetBit(): Int {
        var result = nextSetBitIndex

        if (isNextSetBitIndexDirty) {
            result = findNextSetBitIndex(currentBitIndex)
            nextSetBitIndex = result
            isNextSetBitIndexDirty = false
        }

        return result
    }

    override fun hasNext(): Boolean {
        return findNextSetBit() >= 0
    }

    override fun next(): IntFragmentElement {
        val nextIndex = findNextSetBit()
        if (nextIndex < 0) {
            throw NoSuchElementException()
        }

        currentBitIndex = nextIndex + 1
        isNextSetBitIndexDirty = true

        return IntFragmentElement(nextIndex + limitStart)
    }
}

internal abstract class AbstractBitFragmentIterator(
    initialBitStart: Int,
    private val bitEndIndex: Int,
    private val limitStart: Int
) : ComplexRangeFragmentListIterator<IntFragmentElement> {
    private var _current: RangeFragment<IntFragmentElement>? = null
    override val current: RangeFragment<IntFragmentElement>
        get() = _current ?: throw NoSuchElementException()

    private var lastFragmentStart = initialBitStart - 2
    private var lastFragmentEnd = initialBitStart - 1

    private var markedBitIndex = -1

    override fun moveNext(): Boolean {
        val setBitIndex = findNextSetBitIndex(lastFragmentEnd + 1)
        if (setBitIndex < 0) {
            return false
        }

        var unsetBitIndex = findNextUnsetBitIndex(setBitIndex)
        if (unsetBitIndex < 0) {
            unsetBitIndex = bitEndIndex + 1
        }

        setFragment(setBitIndex, unsetBitIndex - 1)

        return true
    }

    override fun movePrevious(): Boolean {
        val setBitIndex = findPreviousSetBitIndex(lastFragmentStart - 1)
        if (setBitIndex < 0) {
            return false
        }

        var unsetBitIndex = findPreviousUnsetBitIndex(setBitIndex)
        unsetBitIndex = maxOf(unsetBitIndex, -1)

        setFragment(unsetBitIndex + 1, setBitIndex)

        return true
    }

    override fun mark() {
        markedBitIndex = lastFragmentStart
    }

    override fun subRange(): ComplexRange<IntFragmentElement> {
        val markedBitIndex = markedBitIndex
        if (markedBitIndex < 0) {
            throw IllegalStateException("No marked element")
        }

        return createSubRange(markedBitIndex, lastFragmentEnd)
    }

    protected fun setFragment(bitStart: Int, bitEnd: Int) {
        lastFragmentStart = bitStart
        lastFragmentEnd = bitEnd

        _current = IntRangeFragment(bitStart + limitStart, bitEnd + limitStart)
    }

    protected abstract fun findNextSetBitIndex(start: Int): Int
    protected abstract fun findNextUnsetBitIndex(start: Int): Int
    protected abstract fun findPreviousSetBitIndex(start: Int): Int
    protected abstract fun findPreviousUnsetBitIndex(start: Int): Int
    protected abstract fun createSubRange(startIndex: Int, endIndex: Int): ComplexRange<IntFragmentElement>

    fun skipFragments(n: Int) {
        var bitStart = 0
        var rem = n

        while(true) {
            val setBitIndex = findNextSetBitIndex(bitStart)
            if (setBitIndex < 0) {
                throw IllegalStateException("n is greater than the fragment count")
            }

            var unsetBitIndex = findNextUnsetBitIndex(bitStart)
            if (unsetBitIndex < 0) {
                unsetBitIndex = bitEndIndex + 1
            }

            if (rem == 0) {
                setFragment(setBitIndex, unsetBitIndex - 1)
                break
            }

            bitStart = unsetBitIndex + 1
            rem--
        }
    }
}