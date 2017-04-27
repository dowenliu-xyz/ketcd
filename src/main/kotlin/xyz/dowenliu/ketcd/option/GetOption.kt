package xyz.dowenliu.ketcd.option

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.RangeRequest

/**
 * The options for get operation.
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @property endKey The end key of the get request. If it is set, the get request will return the keys from
 * _key_ to _endKey_ (exclusive).
 *
 * If end key is '\u0000', the range is all keys >= key. (--from-key)
 *
 * If the end key is one bit larger than the given key, then it gets all keys with the prefix
 * (the given key). (--prefix)
 *
 * If both key and end key are '\u0000', it returns all keys.
 *
 * @property limit The maximum number of keys to return for a get request. No limit when it's lower than or equal zero.
 * @property revision The revision to use for the get request.
 *  - If the revision is less or equal to zero, the get is over the newest key-value store.
 *  - If the revision has been compacted, ErrCompacted is returned as a response.
 * @property sortOrder Order to sort the returned key value pairs.
 * @property sortTarget Field to sort the key value pairs by the provided [sortOrder].
 * @property serializable Is the get request a serializable get request.
 * Get requests are linearizable by default. For better performance, a serializable get
 * request is served locally without needing to reach consensus with other nodes in the cluster.
 * @property keysOnly Flag to only return keys.
 * @property countOnly Flag to only return count of the keys.
 */
class GetOption private constructor(val endKey: ByteString?,
                                    val limit: Long,
                                    val revision: Long,
                                    val sortOrder: RangeRequest.SortOrder,
                                    val sortTarget: RangeRequest.SortTarget,
                                    val serializable: Boolean,
                                    val keysOnly: Boolean,
                                    val countOnly: Boolean) {
    companion object {
        @JvmStatic val DEFAULT = newBuilder().build()

        /**
         * Create a builder to construct options for get operation.
         *
         * @return builder
         */
        @JvmStatic fun newBuilder(): Builder = Builder()
    }

    class Builder internal constructor() {
        private var limit = 0L
        private var revision = 0L
        private var sortOrder = RangeRequest.SortOrder.NONE
        private var sortTarget = RangeRequest.SortTarget.KEY
        private var serializable = false
        private var keysOnly = false
        private var countOnly = false
        private var endKey: ByteString? = null

        /**
         * Limit the number of keys to return for a get request. By default is 0 - no limitation.
         *
         * @param limit the maximum number of keys to return for a get request.
         * No limit when it's lower than or equal zero.
         * @return this builder to train
         */
        fun withLimit(limit: Long): Builder {
            this.limit = limit
            return this
        }

        /**
         * Provide the revision to use for the get request.
         *  - If the revision is less or equal to zero, the get is over the newest key-value store.
         *  - If the revision has been compacted, ErrCompacted is returned as a response.
         *
         *  @param revision the revision to get.
         *  @return this builder to train.
         */
        fun withRevision(revision: Long): Builder {
            this.revision = revision
            return this
        }

        /**
         * Sort the return key value pairs in the provided _order_
         *
         * @param order order to sort the returned key value pairs
         * @return this builder to train.
         */
        fun withSortOrder(order: RangeRequest.SortOrder): Builder {
            this.sortOrder = order
            return this
        }

        /**
         * Sort the return key value pairs in the provided _field_
         *
         * @param field field to sort the key value pairs by the provided [withSortOrder]
         * @return this builder to train
         */
        fun withSortField(field: RangeRequest.SortTarget): Builder {
            this.sortTarget = field
            return this
        }

        /**
         * Set the get request to be a serializable get request.
         *
         * Get requests are linearizable by default. For better performance, a serializable get
         * request is served locally without needing to reach consensus with other nodes in the cluster.
         *
         * @param serializable is the get request a serializable get request.
         * @return this builder to train.
         */
        fun withSerializable(serializable: Boolean): Builder {
            this.serializable = serializable
            return this
        }

        /**
         * Set the get request to only return keys.
         *
         * @param keysOnly flag to only return keys.
         * @return this builder to train.
         */
        fun withKeysOnly(keysOnly: Boolean): Builder {
            this.keysOnly = keysOnly
            return this
        }

        /**
         * Set the get request to only return count of the keys.
         *
         * @param countOnly flag to only return count of the keys.
         * @return this builder to train.
         */
        fun withCountOnly(countOnly: Boolean): Builder {
            this.countOnly = countOnly
            return this
        }

        /**
         * Set the end key of the get request. If it is set, the get request will return the keys from
         * _key_ to _endKey_ (exclusive).
         *
         * If end key is '\u0000', the range is all keys >= key. (--from-key)
         *
         * If the end key is one bit larger than the given key, then it gets all keys with the prefix
         * (the given key). (--prefix)
         *
         * If both key and end key are '\u0000', it returns all keys.
         *
         * @param endKey end key
         * @return this builder to train.
         */
        fun withRange(endKey: ByteString?): Builder {
            this.endKey = endKey
            return this
        }

        /**
         * Build the get options
         *
         * @return the get options
         */
        fun build(): GetOption = GetOption(endKey, limit, revision, sortOrder, sortTarget, serializable, keysOnly,
                countOnly)
    }
}