package xyz.dowenliu.ketcd.kv

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.Compare

/**
 * The compare predicate
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 */
class Cmp(private val key: ByteString,
          private val op: CmpOp,
          private val target: CmpTarget<*>) {
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

    enum class CmpOp(val result: Compare.CompareResult) {
        EQUAL(Compare.CompareResult.EQUAL),
        GREATER(Compare.CompareResult.GREATER),
        LESS(Compare.CompareResult.LESS)
    }
}