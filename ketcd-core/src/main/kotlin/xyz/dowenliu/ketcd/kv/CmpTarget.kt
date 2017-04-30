package xyz.dowenliu.ketcd.kv

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.Compare

/**
 * Cmp target.
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @param T The compare target value type of this compare.
 *
 * @property target The compare target used for this compare.
 * @property targetValue The compare target value of this compare.
 */
abstract class CmpTarget<out T>(val target: Compare.CompareTarget, val targetValue: T) {
    /**
     * Companion object of [CmpTarget]
     */
    companion object {
        /**
         * ComparePredicate on a given _version_
         *
         * @param version version to compare.
         * @return the version compare target.
         */
        @JvmStatic fun version(version: Long): VersionCmpTarget = VersionCmpTarget(version)

        /**
         * ComparePredicate on the create _revision_
         *
         * @param revision the create revision
         * @return the create revision compare target.
         */
        @JvmStatic fun createVersion(revision: Long): CreateRevisionCmpTarget = CreateRevisionCmpTarget(revision)

        /**
         * ComparePredicate on the modification _revision_
         *
         * @param revision the modification revision
         * @return the modification revision compare target.
         */
        @JvmStatic fun modRevision(revision: Long): ModRevisionCmpTarget = ModRevisionCmpTarget(revision)

        /**
         * ComparePredicate on the _value_
         *
         * @param value the value to compare.
         * @return the value compare target.
         */
        @JvmStatic fun value(value: ByteString): ValueCmpTarget = ValueCmpTarget(value)
    }

    /**
     * [CmpTarget] for [Compare.CompareTarget.VERSION].
     *
     * @param targetValue The [Compare.CompareTarget.VERSION] value to compare with.
     */
    class VersionCmpTarget internal constructor(targetValue: Long) :
            CmpTarget<Long>(Compare.CompareTarget.VERSION, targetValue)

    /**
     * [CmpTarget] for [Compare.CompareTarget.CREATE].
     *
     * @param targetValue The [Compare.CompareTarget.CREATE] value to compare with.
     */
    class CreateRevisionCmpTarget internal constructor(targetValue: Long) :
            CmpTarget<Long>(Compare.CompareTarget.CREATE, targetValue)

    /**
     * [CmpTarget] for [Compare.CompareTarget.MOD].
     *
     * @param targetValue The [Compare.CompareTarget.MOD] value to compare with.
     */
    class ModRevisionCmpTarget internal constructor(targetValue: Long) :
            CmpTarget<Long>(Compare.CompareTarget.MOD, targetValue)

    /**
     * [CmpTarget] for [Compare.CompareTarget.VALUE].
     *
     * @param targetValue The [Compare.CompareTarget.VALUE] value to compare with.
     */
    class ValueCmpTarget internal constructor(targetValue: ByteString) :
            CmpTarget<ByteString>(Compare.CompareTarget.VALUE, targetValue)
}