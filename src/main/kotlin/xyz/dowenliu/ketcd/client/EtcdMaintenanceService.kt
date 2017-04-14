package xyz.dowenliu.ketcd.client

import xyz.dowenliu.ketcd.api.AlarmResponse

/**
 * Interface of maintenance service talking to etcd.
 *
 * An etcd cluster needs periodic maintenance to remain reliable. Depending on an etcd application's needs, this
 * maintenance can usually be automated and performed without downtime or significantly degraded performance.
 *
 * All etcd maintenance manages storage resources consumed by the etcd keyspace. Failure to adequately control the
 * keyspace size is guarded by storage space quotas; if an etcd member runs low on space, a quota will trigger
 * cluster-wide alarms which will put the system into a limited-operation maintenance mode. To avoid running out of
 * space for writes to the keyspace, the etcd keyspace history must be compacted. Storage space itself may be
 * reclaimed by defragmenting etcd members. Finally, periodic snapshot backups of etcd member state makes it possible
 * to recover any unintended logical data loss or corruption caused by operational error.
 *
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 */
interface EtcdMaintenanceService {
    /**
     * Get all active keyspace alarms (blocking).
     *
     * @return [AlarmResponse]
     */
    fun listAlarms(): AlarmResponse

    /**
     * Get all active keyspace alarms (asynchronously).
     *
     * @param callback A [AlarmListCallback] instance to handle the response.
     */
    fun listAlarmsAsync(callback: AlarmListCallback)

    /**
     * Callback when list alarm response received.
     */
    interface AlarmListCallback {
        /**
         * Handle response received.
         *
         * @param response The response received.
         */
        fun onResponse(response: AlarmResponse)

        /**
         * When exception caught
         */
        fun onError(throwable: Throwable)

        /**
         * To complete this callback
         */
        fun completeCallback()
    }
}