package xyz.dowenliu.ketcd.kv.option

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.dowenliu.ketcd.client.EtcdClient
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
        @JvmStatic val DEFAULT = newBuilder().build()

        private val logger: Logger = LoggerFactory.getLogger(PutOption::class.java)

        /**
         * Create a builder to construct options for put operation.
         *
         * @return builder
         */
        @JvmStatic fun newBuilder(): Builder = Builder()
    }

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
            EtcdClient.knowVersion.get()?.let {
                if (it.releaseNumber < EtcdVersion.V3_0_11.releaseNumber)
                    logger.warn("Put option prevKV is support since v3.0.11," +
                            " but current server version can be ${it.value}")
            }
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