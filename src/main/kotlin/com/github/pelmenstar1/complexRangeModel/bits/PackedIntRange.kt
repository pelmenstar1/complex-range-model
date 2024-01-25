package com.github.pelmenstar1.complexRangeModel.bits

private fun packInts(first: Int, end: Int): Long {
    return (first.toLong() and 0xFFFFFFFF) or (end.toLong() shl 32)
}

@JvmInline
value class PackedIntRange private constructor(private val bits: Long) {
    val start: Int
        get() = bits.toInt()

    val endInclusive: Int
        get() = (bits shr 32).toInt()

    constructor(start: Int, end: Int) : this(packInts(start, end))

    operator fun component1(): Int = start
    operator fun component2(): Int = endInclusive

    override fun toString(): String {
        return "[$start, $endInclusive]"
    }

    companion object {
        val Empty = PackedIntRange(0, -1)
    }
}