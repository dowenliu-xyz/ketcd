package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import xyz.dowenliu.ketcd.api.AlarmMember
import xyz.dowenliu.ketcd.api.AlarmResponse
import xyz.dowenliu.ketcd.api.DefragmentResponse
import xyz.dowenliu.ketcd.api.StatusResponse

/**
 * Interface of maintenance service talking to etcd.
 *
 * An etcd cluster needs periodic maintenance to remain reliable. Depending on an etcd application's needs, this
 * maintenance can usually be automated and performed without downtime or significantly degraded performance.
 *
 * All etcd maintenance manages storage resources consumed by the etcd key space. Failure to adequately control the
 * key space size is guarded by storage space quotas; if an etcd member runs low on space, a quota will trigger
 * cluster-wide alarms which will put the system into a limited-operation maintenance mode. To avoid running out of
 * space for writes to the key space, the etcd key space history must be compacted. Storage space itself may be
 * reclaimed by defragmenting etcd members. Finally, periodic snapshot backups of etcd member state makes it possible
 * to recover any unintended logical data loss or corruption caused by operational error.
 *
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 */
interface EtcdMaintenanceService : AutoCloseable {
    /**
     * The client which created this service.
     */
    val client: EtcdClient

    /**
     * Get all active key space alarms (blocking).
     *
     * @return [AlarmResponse]
     */
    fun listAlarms(): AlarmResponse

    /**
     * Get all active key space alarms as future.
     *
     * @return [ListenableFuture] of [AlarmResponse]
     */
    fun listAlarmsInFuture(): ListenableFuture<AlarmResponse>

    /**
     * Get all active key space alarms (asynchronously).
     *
     * @param callback A [ResponseCallback] instance to handle the response.
     */
    fun listAlarmsAsync(callback: ResponseCallback<AlarmResponse>)

    /**
     * Deactive a raised alarm (blocking).
     *
     * @param member The raised alarm to deactive.
     * @return [AlarmResponse]
     */
    fun deactiveAlarm(member: AlarmMember): AlarmResponse

    /**
     * Deactive a raised alarm as future.
     *
     * @param member The raised alarm to deactive.
     * @return [ListenableFuture] of [AlarmResponse]
     */
    fun deactiveAlarmInFuture(member: AlarmMember): ListenableFuture<AlarmResponse>

    /**
     * Deactive a raised alarm (asynchronously).
     * @param member The raised alarm to deactive.
     * @param callback A [ResponseCallback] instance to handle the response.
     */
    fun deactiveAlarmAsync(member: AlarmMember, callback: ResponseCallback<AlarmResponse>)

    /**
     * Defragment one member of the cluster (blocking).
     *
     * After compacting the key space, the backend database may exhibit internal fragmentation. Any internal
     * fragmentation is space that is free to use by the backend but still consumes storage space. The process
     * of defragmentation releases this storage space back to the file system. Defragmentation is issued on a
     * per-member so that cluster-wide latency spikes may be avoided.
     *
     * Defragment is an expensive operation. User should avoid defragmenting multiple members at the same time.
     * To defragment multiple members in the cluster, user need to call defragment multiple times with different
     * endpoints.
     *
     * @return [DefragmentResponse]
     */
    fun defragmentMember(): DefragmentResponse

    /**
     * Defragment one member of the cluster as future.
     *
     * After compacting the key space, the backend database may exhibit internal fragmentation. Any internal
     * fragmentation is space that is free to use by the backend but still consumes storage space. The process
     * of defragmentation releases this storage space back to the file system. Defragmentation is issued on a
     * per-member so that cluster-wide latency spikes may be avoided.
     *
     * Defragment is an expensive operation. User should avoid defragmenting multiple members at the same time.
     * To defragment multiple members in the cluster, user need to call defragment multiple times with different
     * endpoints.
     *
     * @return [ListenableFuture] of [DefragmentResponse]
     */
    fun defragmentMemberInFuture(): ListenableFuture<DefragmentResponse>

    /**
     * Defragment one member of the cluster (asynchronously).
     *
     * After compacting the key space, the backend database may exhibit internal fragmentation. Any internal
     * fragmentation is space that is free to use by the backend but still consumes storage space. The process
     * of defragmentation releases this storage space back to the file system. Defragmentation is issued on a
     * per-member so that cluster-wide latency spikes may be avoided.
     *
     * Defragment is an expensive operation. User should avoid defragmenting multiple members at the same time.
     * To defragment multiple members in the cluster, user need to call defragment multiple times with different
     * endpoints.
     *
     * @param callback A [ResponseCallback] instance to handle the response.
     */
    fun defragmentMemberAsync(callback: ResponseCallback<DefragmentResponse>)

    /**
     * Get the status of one member (blocking).
     *
     * @return [StatusResponse]
     */
    fun statusMember(): StatusResponse

    /**
     * Get the status of one member as future.
     *
     * @return [ListenableFuture] of [StatusResponse]
     */
    fun statusMemberInFuture(): ListenableFuture<StatusResponse>

    /**
     * Get the status of one member (asynchronously).
     *
     * @param callback A [ResponseCallback] instance to handle the response.
     */
    fun statusMemberAsync(callback: ResponseCallback<StatusResponse>)

    // TODO add snapshot operation.
}