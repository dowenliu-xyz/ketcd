package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import xyz.dowenliu.ketcd.api.LeaseGrantResponse
import xyz.dowenliu.ketcd.api.LeaseKeepAliveResponse
import xyz.dowenliu.ketcd.api.LeaseRevokeResponse
import xyz.dowenliu.ketcd.api.LeaseTimeToLiveResponse
import xyz.dowenliu.ketcd.exception.LeaseNotFoundException
import xyz.dowenliu.ketcd.version.EtcdVersion
import xyz.dowenliu.ketcd.version.ForEtcdVersion

/**
 * Interface of lease service talking to etcd.
 *
 * create at 2017/4/26
 * @author liufl
 * @since 0.1.0
 */
interface EtcdLeaseService : AutoCloseable {
    /**
     * The client which created this service.
     */
    val client: EtcdClient

    /**
     * New or re-grant a lease with ttl value (blocking).
     *
     * @param ttl TTL value, unit seconds.
     * @param leaseId If the lease id is 0, the lessor chooses an ID.
     * If the specified id do not exist, it will be create.
     * @return [LeaseGrantResponse]
     */
    fun grant(ttl: Long, leaseId: Long = 0L): LeaseGrantResponse

    /**
     * New or re-grant a lease with ttl value in future.
     *
     * @param ttl TTL value, unit seconds.
     * @param leaseId If the lease id is 0, the lessor chooses an ID.
     * If the specified id do not exist, it will be create.
     * @return [ListenableFuture] of [LeaseGrantResponse]
     */
    fun grantInFuture(ttl: Long, leaseId: Long = 0L): ListenableFuture<LeaseGrantResponse>


    /**
     * New or re-grant a lease with ttl value (asynchronously).
     *
     * @param ttl TTL value, unit seconds.
     * @param leaseId If the lease id is 0, the lessor chooses an ID.
     * If the specified id do not exist, it will be create.
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun grantAsync(ttl: Long, leaseId: Long = 0L, callback: ResponseCallback<LeaseGrantResponse>)

    /**
     * Retrieves lease information (blocking).
     *
     * @param leaseId The id of the lease to retrieve.
     * @param withKeys If the keys attached to the lease should return in the response.
     * @return [LeaseTimeToLiveResponse]
     */
    @ForEtcdVersion(EtcdVersion.V3_1_0_alpha0)
    fun timeToLive(leaseId: Long, withKeys: Boolean = false): LeaseTimeToLiveResponse

    /**
     * Retrieves lease information in future.
     *
     * @param leaseId The id of the lease to retrieve.
     * @param withKeys If the keys attached to the lease should return in the response.
     * @return [ListenableFuture] of [LeaseTimeToLiveResponse]
     */
    @ForEtcdVersion(EtcdVersion.V3_1_0_alpha0)
    fun timeToLiveInFuture(leaseId: Long, withKeys: Boolean = false): ListenableFuture<LeaseTimeToLiveResponse>

    /**
     * Retrieves lease information (asynchronously).
     *
     * @param leaseId The id of the lease to retrieve.
     * @param withKeys If the keys attached to the lease should return in the response.
     * @return [LeaseTimeToLiveResponse]
     */
    @ForEtcdVersion(EtcdVersion.V3_1_0_alpha0)
    fun timeToLiveAsync(leaseId: Long, withKeys: Boolean = false, callback: ResponseCallback<LeaseTimeToLiveResponse>)

    /**
     * Revoke one lease and also remove all keys attached to the lease (blocking).
     *
     * @param leaseId The id of the lease to revoke.
     * @return [LeaseRevokeResponse]
     */
    fun revoke(leaseId: Long): LeaseRevokeResponse

    /**
     * Revoke one lease and also remove all keys attached to the lease in future.
     *
     * @param leaseId The id of the lease to revoke.
     * @return [ListenableFuture] of [LeaseRevokeResponse]
     */
    fun revokeInFuture(leaseId: Long): ListenableFuture<LeaseRevokeResponse>

    /**
     * Revoke one lease and also remove all keys attached to the lease (asynchronously).
     *
     * @param leaseId The id of the lease to revoke.
     * @param callback A [ResponseCallback] to handle the response
     */
    fun revokeAsync(leaseId: Long, callback: ResponseCallback<LeaseRevokeResponse>)

    /**
     * Keep alive one lease.
     *
     * @param leaseId The id of the lease to keep alive.
     * @param eventHandler User defined handler to handle the keepAlive event.
     * Note that you should not cancel a keepAlive sentinel by this event handler.
     * If you do not need do anything, just leave this handler _null_
     * @return A [KeepAliveSentinel]
     * @throws LeaseNotFoundException If the lease specified is not found
     */
    @Throws(LeaseNotFoundException::class)
    fun keepAlive(leaseId: Long, eventHandler: KeepAliveEventHandler? = null): KeepAliveSentinel

    /**
     * This interface describes a sentinel object keeps the keep alive stream for a lease.
     *
     * Usually a sentinel will begin heart beat once it it created.
     * If the lease specified is not found, a [xyz.dowenliu.ketcd.exception.LeaseNotFoundException] throws.
     *
     * When you need stop this keepAlive, call the [close] function. This [close] function will also be called before gc.
     */
    interface KeepAliveSentinel: AutoCloseable {
        /**
         * @return If this sentinel is closed by [close]
         */
        fun isClosed(): Boolean
    }

    /**
     * This interface allow user to do something when keepAlive works ok or encounters an exception.
     */
    interface KeepAliveEventHandler {
        /**
         * Called when heartbeat ok.
         *
         * @param response A normal response that received.
         */
        fun onResponse(response: LeaseKeepAliveResponse)

        /**
         * Called when a exception occurred.
         *
         * @param throwable The exception caught.
         */
        fun onError(throwable: Throwable)
    }
}