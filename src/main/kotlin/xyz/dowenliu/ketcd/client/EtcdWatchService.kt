package xyz.dowenliu.ketcd.client

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.WatchResponse
import xyz.dowenliu.ketcd.option.WatchOption

/**
 * Interface of watch service talking to etcd.
 *
 * create at 2017/4/27
 * @author liufl
 * @since 0.1.0
 */
interface EtcdWatchService {
    /**
     * The client which created this service.
     */
    val client: EtcdClient

    /**
     * Watch watches for events happening or that have happened.
     * The entire event history can be watched starting from the last compaction revision.
     *
     * @param key Watch key range start key.
     * If _endKey_ of [watchOption] is [ByteString.EMPTY], the watch just work on the single key.
     * @param watchOption Watch options. See [WatchOption] for details.
     * @param handler Handler for user to do something with the watch event from server side.
     * @return A sentinel to control the watch.
     */
    fun watch(key: ByteString, watchOption: WatchOption, handler: WatchEventHandler): WatchSentinel

    /**
     * A sentinel to cantrol the watch.
     */
    interface WatchSentinel : AutoCloseable {
        fun isClosed(): Boolean
    }

    /**
     * A handler for user to do something with the watch event from the server side.
     *
     * Not that the [onCompleted] function may not work with etcd servers since V3.1.0
     * (But all v3.1.0 alpha and RC releases work fine.).
     */
    interface WatchEventHandler {
        /**
         * Called when a normal watch response received.
         */
        fun onResponse(response: WatchResponse)

        /**
         * Called when an error caught.
         */
        fun onError(throwable: Throwable)

        /**
         * Called when the watch stream is completed and closing.
         *
         * May not work with etcd servers since V3.1.0.
         */
        fun onCompleted()
    }
}