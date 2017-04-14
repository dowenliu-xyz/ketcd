package xyz.dowenliu.ketcd.client

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import xyz.dowenliu.ketcd.api.AlarmRequest
import xyz.dowenliu.ketcd.api.AlarmResponse
import xyz.dowenliu.ketcd.api.AlarmType
import xyz.dowenliu.ketcd.api.MaintenanceGrpc
import java.io.Closeable

/**
 * Implementation of maintenance service.
 *
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
class EtcdMaintenanceServiceImpl internal constructor(val channel: ManagedChannel, val token: String?) :
        EtcdMaintenanceService, Closeable {
    private val blockingStub = configureStub(MaintenanceGrpc.newBlockingStub(channel), token)
    private val asyncStub = configureStub(MaintenanceGrpc.newStub(channel), token)

    override fun close() {
        channel.shutdownNow()
    }

    private fun listAlarmsRequest() = AlarmRequest.newBuilder()
            .setAlarm(AlarmType.NONE)
            .setAction(AlarmRequest.AlarmAction.GET)
            .setMemberID(0)
            .build()

    override fun listAlarms(): AlarmResponse = blockingStub.alarm(listAlarmsRequest())

    override fun listAlarmsAsync(callback: EtcdMaintenanceService.AlarmListCallback) {
        asyncStub.alarm(listAlarmsRequest(), object : StreamObserver<AlarmResponse> {
            override fun onNext(value: AlarmResponse?) {
                value?.let { callback.onResponse(it) }
            }

            override fun onError(t: Throwable?) {
                t?.let { callback.onError(it) }
            }

            override fun onCompleted() = callback.completeCallback()
        })
    }
}