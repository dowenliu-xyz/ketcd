package xyz.dowenliu.ketcd.option

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.WatchCreateRequest
import xyz.dowenliu.ketcd.kv.FROM_KEY
import xyz.dowenliu.ketcd.kv.NULL_KEY
import xyz.dowenliu.ketcd.kv.prefixKeyOf
import xyz.dowenliu.ketcd.version.EtcdVersion
import xyz.dowenliu.ketcd.version.ForEtcdVersion
import java.util.*

/**
 * The options for watch operation
 *
 * create at 2017/4/27
 * @author liufl
 * @since 0.1.0
 * @property endKey The end key of the watch stream. If it is not [ByteString.EMPTY],
 * the watch stream will watch the keys from _key_ to _endKey_ (exclusive).
 *
 * If end key is '\u0000' ([FROM_KEY]), the range is all keys >= key. (--from-key)
 *
 * If the end key is one bit larger than the given key, then it gets all keys with the prefix
 * (the given key). (--prefix). You can get it with [prefixKeyOf] function.
 *
 * If both key and end key are '\u0000' ([NULL_KEY] and [FROM_KEY]), it returns all keys.
 * @property startRevision The revision to watch from (startRevision).
 * Less than or equals zero will watch from 'now'.
 * @property progressNotify progress_notify is set so that the etcd server will periodically send a WatchResponse with
 * no events to the new watcher if there are no recent events. It is useful when clients
 * wish to recover a disconnected watcher starting from a recent known revision.
 * The etcd server may decide how often it will send notifications based on current load.
 * @property prevKV If prev_kv is set, created watcher gets the previous KV before the event happens.
 * If the previous KV is already compacted, nothing will be returned.
 * @property filters filters filter the events at server side before it sends back to the watcher.
 */
class WatchOption private constructor(val endKey: ByteString,
                                      val startRevision: Long,
                                      val progressNotify: Boolean,
                                      @ForEtcdVersion(EtcdVersion.V3_0_12) val prevKV: Boolean,
                                      @ForEtcdVersion(EtcdVersion.V3_1_0_alpha0) val filters: List<WatchCreateRequest.FilterType>) {
    /**
     * Companion object of [WatchOption]
     */
    companion object {
        /**
         * The default watch options.
         */
        @JvmStatic val DEFAULT = newBuilder().build()

        /**
         * Create a builder to construct options for watch operation.
         *
         * @return builder
         */
        @JvmStatic fun newBuilder(): Builder = Builder()
    }

    /**
     * Builder to construct [WatchOption].
     */
    class Builder internal constructor() {
        private var endKey: ByteString = ByteString.EMPTY
        private var startRevision: Long = 0L
        private var progressNotify: Boolean = false
        @ForEtcdVersion(EtcdVersion.V3_0_12) private var prevKV: Boolean = false
        @ForEtcdVersion(EtcdVersion.V3_1_0_alpha0) private val filters: MutableList<WatchCreateRequest.FilterType> =
                Collections.synchronizedList(mutableListOf())

        /**
         * Set the end key of the watch stream. If it is not [ByteString.EMPTY],
         * the watch stream will watch the keys from _key_ to _endKey_ (exclusive).
         *
         * If end key is '\u0000' ([FROM_KEY]), the range is all keys >= key. (--from-key)
         *
         * If the end key is one bit larger than the given key, then it gets all keys with the prefix
         * (the given key). (--prefix). You can get it with [prefixKeyOf] function.
         *
         * If both key and end key are '\u0000' ([NULL_KEY] and [FROM_KEY]), it returns all keys.
         *
         * @param endKey end key
         * @return this builder to train.
         */
        fun withRange(endKey: ByteString): Builder {
            this.endKey = endKey
            return this
        }

        /**
         * Set the revision to watch from (inclusive). Less than or equals zero will watch from 'now'.
         * // TEST_THIS can be less than zero
         *
         * @param revision The revision to watch from (inclusive).
         * @return this builder to train.
         */
        fun withStartRevision(revision: Long): Builder {
            this.startRevision = revision
            return this
        }

        /**
         * progress_notify is set so that the etcd server will periodically send a WatchResponse with
         * no events to the new watcher if there are no recent events. It is useful when clients
         * wish to recover a disconnected watcher starting from a recent known revision.
         * The etcd server may decide how often it will send notifications based on current load.
         *
         * @param progressNotify progress_notify flag.
         * @return this builder to train.
         */
        fun withProgressNotify(progressNotify: Boolean): Builder {
            this.progressNotify = progressNotify
            return this
        }

        /**
         * If prev_kv is set, created watcher gets the previous KV before the event happens.
         * If the previous KV is already compacted, nothing will be returned.
         *
         * @param prevKV prev_kv flag.
         * @return this builder to train.
         */
        @ForEtcdVersion(EtcdVersion.V3_0_12)
        fun withPrevKV(prevKV: Boolean): Builder {
            this.prevKV = prevKV
            return this
        }

        /**
         * filters filter the events at server side before it sends back to the watcher.
         *
         * @param filter Filters
         * @return this builder to train.
         */
        @ForEtcdVersion(EtcdVersion.V3_1_0_alpha0)
        fun withFilter(vararg filter: WatchCreateRequest.FilterType): Builder {
            this.filters.addAll(filter)
            return this
        }

        /**
         * Build the watch option
         *
         * @return The watch option
         */
        fun build(): WatchOption = WatchOption(endKey, startRevision, progressNotify, prevKV, filters.distinct())
    }
}