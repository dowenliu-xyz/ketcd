package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.client.EtcdLeaseService.KeepAliveEventHandler
import xyz.dowenliu.ketcd.client.EtcdLeaseService.KeepAliveSentinel
import xyz.dowenliu.ketcd.exception.LeaseNotFoundException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Implementation of etcd lease service.
 *
 * create at 2017/4/26
 * @author liufl
 * @since 0.1.0
 */
class EtcdLeaseServiceImpl internal constructor(override val client: EtcdClient) : EtcdLeaseService {
    /**
     * Companion object of [EtcdLeaseServiceImpl]
     */
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EtcdLeaseServiceImpl::class.java)
        private val keepAliveThreadGroup = ThreadGroup("lease-keep-alive")
        private val threadCounter = AtomicLong()
    }

    private val channel: ManagedChannel = client.channelBuilder.build()
    private val blockingStub = configureStub(LeaseGrpc.newBlockingStub(channel), client.token)
    private val futureStub = configureStub(LeaseGrpc.newFutureStub(channel), client.token)
    private val asyncStub = configureStub(LeaseGrpc.newStub(channel), client.token)

    /**
     * Close this service instance.
     */
    override fun close() {
        channel.shutdownNow()
    }

    private fun grantRequest(ttl: Long, leaseId: Long): LeaseGrantRequest =
            LeaseGrantRequest.newBuilder().setTTL(ttl).setID(leaseId).build()

    override fun grant(ttl: Long, leaseId: Long): LeaseGrantResponse =
            blockingStub.leaseGrant(grantRequest(ttl, leaseId))

    override fun grantInFuture(ttl: Long, leaseId: Long): ListenableFuture<LeaseGrantResponse> =
            futureStub.leaseGrant(grantRequest(ttl, leaseId))

    override fun grantAsync(ttl: Long, leaseId: Long, callback: ResponseCallback<LeaseGrantResponse>) =
            asyncStub.leaseGrant(grantRequest(ttl, leaseId), CallbackStreamObserver(callback))

    private fun timeToLiveRequest(leaseId: Long, withKeys: Boolean): LeaseTimeToLiveRequest =
            LeaseTimeToLiveRequest.newBuilder().setID(leaseId).setKeys(withKeys).build()

    override fun timeToLive(leaseId: Long, withKeys: Boolean): LeaseTimeToLiveResponse =
            blockingStub.leaseTimeToLive(timeToLiveRequest(leaseId, withKeys))

    override fun timeToLiveInFuture(leaseId: Long, withKeys: Boolean): ListenableFuture<LeaseTimeToLiveResponse> =
            futureStub.leaseTimeToLive(timeToLiveRequest(leaseId, withKeys))

    override fun timeToLiveAsync(leaseId: Long,
                                 withKeys: Boolean,
                                 callback: ResponseCallback<LeaseTimeToLiveResponse>) =
            asyncStub.leaseTimeToLive(timeToLiveRequest(leaseId, withKeys), CallbackStreamObserver(callback))

    override fun revoke(leaseId: Long): LeaseRevokeResponse =
            blockingStub.leaseRevoke(LeaseRevokeRequest.newBuilder().setID(leaseId).build())

    override fun revokeInFuture(leaseId: Long): ListenableFuture<LeaseRevokeResponse> =
            futureStub.leaseRevoke(LeaseRevokeRequest.newBuilder().setID(leaseId).build())

    override fun revokeAsync(leaseId: Long, callback: ResponseCallback<LeaseRevokeResponse>) =
            asyncStub.leaseRevoke(LeaseRevokeRequest.newBuilder().setID(leaseId).build(),
                    CallbackStreamObserver(callback))

    override fun keepAlive(leaseId: Long, eventHandler: KeepAliveEventHandler?): KeepAliveSentinel =
            MyKeepAliveSentinel(leaseId, eventHandler)

    private inner class MyKeepAliveSentinel(val leaseId: Long,
                                            val eventHandler: KeepAliveEventHandler?) : KeepAliveSentinel {
        private var closed: Boolean = false
        private var ttlSignal: BlockingQueue<Long> = ArrayBlockingQueue(1)
        private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor {
            Thread(keepAliveThreadGroup, it, "lease-$leaseId-keep-alive-thread-${threadCounter.incrementAndGet()}")
            // The thread should not be daemon, JVM stop after this thread stop.
        }
        private val keepAliveResponseStreamObserver: StreamObserver<LeaseKeepAliveResponse> =
                object : StreamObserver<LeaseKeepAliveResponse> {
                    override fun onNext(value: LeaseKeepAliveResponse?) {
                        if (isClosed()) return
                        value?.let {
                            if (logger.isDebugEnabled)
                                logger.debug("Lease [$leaseId] keep alive response: $it")
                            ttlSignal.clear()
                            ttlSignal.put(it.ttl)
                            try {
                                scheduledExecutor.schedule({ heartbeat() }, it.ttl / 2 + 1, TimeUnit.SECONDS)
                            } catch (e: RejectedExecutionException) {
                                if (!isClosed()) throw e
                            }
                            eventHandler?.onResponse(it)
                        }
                    }

                    override fun onError(t: Throwable?) {
                        t?.let {
                            logger.debug("Lease [$leaseId] keep alive error:", t)
                            eventHandler?.onError(it)
                        }
                    }

                    override fun onCompleted() {
                        logger.debug("Lease [$leaseId] keep alive stream closed.")
                    }
                }
        private val keepAliveRequestStreamObserver: StreamObserver<LeaseKeepAliveRequest> =
                asyncStub.leaseKeepAlive(keepAliveResponseStreamObserver)

        init {
            heartbeat()
            val ttl = ttlSignal.take()
            if (ttl <= 0L) {
                close()
                throw LeaseNotFoundException("Can not find lease with it $leaseId, maybe it's expired yet.")
            }
        }

        private fun heartbeat() {
            this.keepAliveRequestStreamObserver.onNext(LeaseKeepAliveRequest.newBuilder().setID(leaseId).build())
        }

        @Synchronized
        override fun isClosed(): Boolean = closed

        @Synchronized
        override fun close() {
            this.keepAliveRequestStreamObserver.onCompleted()
            this.scheduledExecutor.shutdown()
            closed = true
        }
    }
}