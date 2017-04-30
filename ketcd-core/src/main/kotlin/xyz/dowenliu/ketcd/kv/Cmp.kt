package xyz.dowenliu.ketcd.kv

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.Compare

/**
 * The compare predicate
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @property key Compare key
 * @property op CmpOp
 * @property target Compare target.
 */
class Cmp(private val key: ByteString,
          private val op: CmpOp,
          private val target: CmpTarget<*>) {
    /**
     * Predicate a [Compare] using in deeper gRPC APIs.
     *
     * @return A [Compare] predicated.
     */
    fun toCompare(): Compare {
        val builder = Compare.newBuilder().setKey(key)
                .setResult(op.result)
                .setTarget(target.target)
        when (target) {
            is CmpTarget.VersionCmpTarget -> builder.version = target.targetValue
            is CmpTarget.ValueCmpTarget -> builder.value = target.targetValue
            is CmpTarget.ModRevisionCmpTarget -> builder.modRevision = target.targetValue
            is CmpTarget.CreateRevisionCmpTarget -> builder.createRevision = target.targetValue
            else -> throw IllegalArgumentException("Unexpected target type ($target)")
        }
        return builder.build()
    }

    /**
     * A sub collection of [Compare.CompareResult].
     *
     * In a Txn operation, we only do EQUAL, GREATER and LESS compare.
     *
     * @property result The [Compare.CompareResult] wrapping.
     */
    enum class CmpOp(val result: Compare.CompareResult) {
        /**
         * A wrapper for [Compare.CompareResult.EQUAL]
         */
        EQUAL(Compare.CompareResult.EQUAL),
        /**
         * A wrapper for [Compare.CompareResult.GREATER]
         */
        GREATER(Compare.CompareResult.GREATER),

        /**
         * A wrapper for [Compare.CompareResult.LESS]
         */
        LESS(Compare.CompareResult.LESS)
    }
}