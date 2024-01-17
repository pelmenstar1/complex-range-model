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
    constructor(range: IntRange): this(range.first, range.last)

    operator fun component1(): Int = start
    operator fun component2(): Int = endInclusive

    fun toIntRange(): IntRange = start..endInclusive

    override fun toString(): String {
        return "[$start, $endInclusive]"
    }
}