package com.github.pelmenstar1.complexRangeModel

internal class ComplexRangeElementCollection<T : FragmentElement<T>>(
    private val fragments: List<RangeFragment<T>>
) : Collection<T> {
    private var _size = -1
    override val size: Int
        get() {
            var s = _size
            if (s < 0) {
                s = computeSize()
                _size = s
            }

            return s
        }

    private fun computeSize(): Int {
        var result = 0
        fragments.forEach {
            result += it.elementCount
        }

        return result
    }

    override fun isEmpty(): Boolean {
        return fragments.isEmpty()
    }

    override fun contains(element: T): Boolean {
        fragments.forEach {
            if (element in it) {
                return true
            }
        }

        return false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun iterator(): Iterator<T> = IteratorImpl()

    private inner class IteratorImpl : Iterator<T> {
        private var currentFragment: RangeFragment<T>? = null
        private var lastReturnedElement: T? = null

        private val fragmentIterator = fragments.iterator()

        override fun hasNext(): Boolean {
            val cf = currentFragment

            return if (cf == null || lastReturnedElement == cf.endInclusive) {
                fragmentIterator.hasNext()
            } else {
                true
            }
        }

        override fun next(): T {
            var cf = currentFragment
            var le = lastReturnedElement

            if (cf == null || le == cf.endInclusive) {
                cf = fragmentIterator.next()
                currentFragment = cf

                le = cf.start
            } else {
                le = le?.next() ?: cf.start
            }

            lastReturnedElement = le
            return le
        }

    }


}