package xyz.dowenliu.ketcd.option

import xyz.dowenliu.ketcd.version.EtcdVersion
import xyz.dowenliu.ketcd.version.ForEtcdVersion

/**
 * The options for put operation
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @property leaseId The lease id bind to.
 * @property prevKV If the response will contains previous key-value pair.
 * @property ignoreValue updates the key using its current value
 * @property ignoreLease updates the key using its current lease
 */
class PutOption private constructor(val leaseId: Long,
                                    @ForEtcdVersion(EtcdVersion.V3_0_11) val prevKV: Boolean,
                                    @ForEtcdVersion(EtcdVersion.V3_2_0_rc0) val ignoreValue: Boolean,
                                    @ForEtcdVersion(EtcdVersion.V3_2_0_rc0) val ignoreLease: Boolean) {
    /**
     * Companion object of [PutOption]
     */
    companion object {
        /**
         * The default put options.
         */
        @JvmStatic val DEFAULT = newBuilder().build()

        /**
         * Create a builder to construct options for put operation.
         *
         * @return builder
         */
        @JvmStatic fun newBuilder(): Builder = Builder()
    }

    /**
     * Builder to construct [PutOption].
     */
    class Builder internal  constructor() {
        private var leaseId = 0L
        private var prevKV = false
        private var ignoreValue = false
        private var ignoreLease = false

        /**
         * Assign a _leaseId_ for a put operation. Zero means no lease.
         *
         * @param leaseId lease id to apply to a put operation
         * @return this builder to train
         * @throws IllegalArgumentException if lease is less than zero.
         */
        fun withLeaseId(leaseId: Long): Builder {
            require(leaseId >= 0, { "leaseId should greater than or equal to zero: leaseId=$leaseId" })
            this.leaseId = leaseId
            return this
        }

        /**
         * Set if response contains previous key-value pair.
         *
         * @param prevKV response will contains previous key-value pair if true.
         * @return this builder to train
         */
        @ForEtcdVersion(EtcdVersion.V3_0_11)
        fun prevKV(prevKV: Boolean = true): Builder {
            this.prevKV = prevKV
            return this
        }

        /**
         * Updates the key using its current value (ignore given value)
         *
         * @param ignoreValue if updates the key using its current value.
         * @return this builder to train.
         */
        @ForEtcdVersion(EtcdVersion.V3_2_0_rc0)
        fun ignoreValue(ignoreValue: Boolean = true): Builder {
            this.ignoreValue = ignoreValue
            return this
        }

        /**
         * Updates the key using its current lease
         *
         * @param ignoreLease if updates the key using its current lease.
         * @return this builder to train.
         */
        @ForEtcdVersion(EtcdVersion.V3_2_0_rc0)
        fun ignoreLease(ignoreLease: Boolean = true): Builder {
            this.ignoreLease = ignoreLease
            return this
        }

        /**
         * Build the put option
         *
         * @return the put option
         */
        fun build(): PutOption = PutOption(leaseId, prevKV, ignoreValue, ignoreLease)
    }
}