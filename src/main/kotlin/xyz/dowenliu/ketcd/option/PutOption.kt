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
 */
// TODO ignore-value
// TODO ignore-lease
class PutOption private constructor(val leaseId: Long,
                                    @ForEtcdVersion(EtcdVersion.V3_0_11) val prevKV: Boolean) {
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
         * Build the put option
         *
         * @return the put option
         */
        fun build(): PutOption = PutOption(leaseId, prevKV)
    }
}