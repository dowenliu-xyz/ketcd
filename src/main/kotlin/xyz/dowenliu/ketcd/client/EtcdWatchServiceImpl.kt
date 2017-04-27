package xyz.dowenliu.ketcd.client

import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.option.WatchOption
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Implementation of etcd watch service.
 *
 * create at 2017/4/27
 * @author liufl
 * @since 0.1.0
 */
class EtcdWatchServiceImpl internal constructor(override val client: EtcdClient) : EtcdWatchService {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val channel: ManagedChannel = client.channelBuilder.build()
    private val stub = configureStub(WatchGrpc.newStub(channel), client.token)

    override fun watch(key: ByteString,
                       watchOption: WatchOption,
                       handler: EtcdWatchService.WatchEventHandler): EtcdWatchService.WatchSentinel =
            MyWatchSentinel(key, watchOption, handler)

    private inner class MyWatchSentinel(key: ByteString,
                                        watchOption: WatchOption,
                                        handler: EtcdWatchService.WatchEventHandler) :
            EtcdWatchService.WatchSentinel {
        private var closed: Boolean = false
        private var watchIdSignal: BlockingQueue<Long> = ArrayBlockingQueue(1)
        private val watchResponseStreamObserver: StreamObserver<WatchResponse> =
                object : StreamObserver<WatchResponse> {
                    override fun onNext(value: WatchResponse?) {
                        value?.let {
                            if (it.created) {
                                watchIdSignal.put(it.watchId)
                            }
                            handler.onResponse(it)
                        }
                    }

                    override fun onError(t: Throwable?) {
                        t?.let { handler.onError(it) }
                    }

                    override fun onCompleted() {
                        handler.onCompleted()
                    }
                }
        private val watchRequestStreamObserver: StreamObserver<WatchRequest> = stub.watch(watchResponseStreamObserver)
        private val watchId: Long

        init {
            val builder = WatchCreateRequest.newBuilder()
                    .setKey(key)
                    .setRangeEnd(watchOption.endKey)
                    .setStartRevision(watchOption.startRevision)
                    .setProgressNotify(watchOption.progressNotify)
                    .setPrevKv(watchOption.prevKV)
                    .addAllFilters(watchOption.filters)
            watchRequestStreamObserver.onNext(WatchRequest.newBuilder().setCreateRequest(builder).build())
            watchId = watchIdSignal.take()
        }

        @Synchronized
        override fun isClosed(): Boolean = closed

        @Synchronized
        override fun close() {
            val builder = WatchCancelRequest.newBuilder().setWatchId(watchId)
            watchRequestStreamObserver.onNext(WatchRequest.newBuilder().setCancelRequest(builder).build())
            watchRequestStreamObserver.onCompleted()
        }
    }
}