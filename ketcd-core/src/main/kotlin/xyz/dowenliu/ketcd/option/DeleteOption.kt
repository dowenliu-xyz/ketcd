package xyz.dowenliu.ketcd.option

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.kv.FROM_KEY
import xyz.dowenliu.ketcd.kv.NULL_KEY
import xyz.dowenliu.ketcd.kv.prefixKeyOf
import xyz.dowenliu.ketcd.version.EtcdVersion
import xyz.dowenliu.ketcd.version.ForEtcdVersion

/**
 * The options for delete operation.
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @property endKey The end key of the delete request. If it is not [ByteString.EMPTY],
 * the delete request will delete the keys from _key_ to _endKey_ (exclusive).
 *
 * If end key is '\u0000' ([FROM_KEY]), the range is all keys >= key. (--from-key)
 *
 * If the end key is one bit larger than the given key, then it deletes all keys with the prefix
 * (the given key). (--prefix). You can get it with [prefixKeyOf] function.
 *
 * If both key and end key are '\u0000' ([NULL_KEY] and [FROM_KEY]), it deletes all keys.
 * @property prevKV Flag to get previous key/value pairs before deleting them.
 */
class DeleteOption private constructor(val endKey: ByteString,
                                       @ForEtcdVersion(EtcdVersion.V3_0_11) val prevKV: Boolean) {
    /**
     * Companion object of [DeleteOption]
     */
    companion object {
        /**
         * The default delete options.
         */
        @JvmStatic val DEFAULT = newBuilder().build()

        /**
         * Create a builder to construct options for get operation.
         *
         * @return builder
         */
        @JvmStatic fun newBuilder(): Builder = Builder()
    }

    /**
     * Builder to construct [DeleteOption].
     */
    class Builder internal constructor() {
        private var endKey: ByteString = ByteString.EMPTY
        private var prevKV = false

        /**
         * Set the end key of the delete request. If it is not [ByteString.EMPTY],
         * the delete request will delete the keys from _key_ to _endKey_ (exclusive).
         *
         * If end key is '\u0000' ([FROM_KEY]), the range is all keys >= key. (--from-key)
         *
         * If the end key is one bit larger than the given key, then it deletes all keys with the prefix
         * (the given key). (--prefix). You can get it with [prefixKeyOf] function.
         *
         * If both key and end key are '\u0000' ([NULL_KEY] and [FROM_KEY]), it deletes all keys.
         *
         * @param endKey end key
         * @return this builder to train.
         */
        fun withRange(endKey: ByteString): Builder {
            this.endKey = endKey
            return this
        }

        /**
         * Get the previous key/value pairs before deleting them.
         *
         * @param prevKV flag to get previous key/value pairs before deleting them.
         * @return this builder to train.
         */
        @ForEtcdVersion(EtcdVersion.V3_0_11)
        fun withPrevKV(prevKV: Boolean): Builder {
            this.prevKV = prevKV
            return this
        }

        /**
         * Build the delete options
         *
         * @return the delete options
         */
        fun build(): DeleteOption = DeleteOption(endKey, prevKV)
    }
}