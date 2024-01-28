package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*

class GenericComplexRange<T : FragmentElement<T>> internal constructor(
    private val fragments: RawLinkedList<RangeFragment<T>>
) : ComplexRange<T> {
    override fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T> {
        val copied = fragments.copyOf()
        GenericComplexRangeModify(copied).also(block)

        return GenericComplexRange(copied)
    }

    override fun fragments(): List<RangeFragment<T>> {
        return fragments
    }

    override fun equals(other: Any?): Boolean {
        return when(other) {
            is GenericComplexRange<*> -> fragments == other.fragments
            is ComplexRange<*> -> fragments.sequenceEquals(other.fragments())
            else -> false
        }
    }

    override fun hashCode(): Int {
        return fragments.hashCode()
    }

    override fun toString(): String {
        return buildString {
            append("ComplexRange(")

            var isFirst = true
            fragments.forEachForward { frag ->
                if (!isFirst) {
                    append(", ")
                }

                isFirst = false
                append(frag)
            }

            append(')')
        }
    }

    companion object {
        fun <T : FragmentElement<T>> empty() = GenericComplexRange<T>(RawLinkedList())
    }
}

class GenericComplexRangeBuilder<T : FragmentElement<T>> : GenericComplexRangeBaseBuilder<T>(), ComplexRangeBuilder<T> {
    override fun fragment(value: RangeFragment<T>) {
        includeFragment(value)
    }

    fun build(): ComplexRange<T> {
        return GenericComplexRange(fragments)
    }
}

class GenericComplexRangeModify<T : FragmentElement<T>>(
    fragments: RawLinkedList<RangeFragment<T>>
) : GenericComplexRangeBaseBuilder<T>(fragments), ComplexRangeModify<T> {
    override fun set(fragment: RangeFragment<T>) {
        includeFragment(fragment)
    }

    override fun unset(fragment: RangeFragment<T>) {
        excludeFragment(fragment)
    }
}