package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import xyz.dowenliu.ketcd.api.*
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
    private val futureStub = configureStub(MaintenanceGrpc.newFutureStub(channel), token)
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

    override fun listAlarmsInFuture(): ListenableFuture<AlarmResponse> = futureStub.alarm(listAlarmsRequest())

    override fun listAlarmsAsync(callback: ResponseCallback<AlarmResponse>) =
            asyncStub.alarm(listAlarmsRequest(), object : StreamObserver<AlarmResponse> {
                override fun onNext(value: AlarmResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })

    private fun deactiveAlarmRequest(member: AlarmMember): AlarmRequest {
        val memberID = member.memberID
        require(memberID != 0L, { "The member id can not be 0." })
        require(member.alarm != AlarmType.NONE, { "Alarm type is NONE." })
        return AlarmRequest.newBuilder()
                .setAlarm(AlarmType.NOSPACE)
                .setAction(AlarmRequest.AlarmAction.DEACTIVATE)
                .setMemberID(memberID)
                .build()
    }

    override fun deactiveAlarm(member: AlarmMember): AlarmResponse = blockingStub.alarm(deactiveAlarmRequest(member))

    override fun deactiveAlarmInFuture(member: AlarmMember): ListenableFuture<AlarmResponse> =
            futureStub.alarm(deactiveAlarmRequest(member))

    override fun deactiveAlarmAsync(member: AlarmMember, callback: ResponseCallback<AlarmResponse>) =
            asyncStub.alarm(deactiveAlarmRequest(member), object : StreamObserver<AlarmResponse> {
                override fun onNext(value: AlarmResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })

    override fun defragmentMember(): DefragmentResponse =
            blockingStub.defragment(DefragmentRequest.getDefaultInstance())

    override fun defragmentMemberInFuture(): ListenableFuture<DefragmentResponse> =
            futureStub.defragment(DefragmentRequest.getDefaultInstance())

    override fun defragmentMemberAsync(callback: ResponseCallback<DefragmentResponse>) =
            asyncStub.defragment(DefragmentRequest.getDefaultInstance(), object : StreamObserver<DefragmentResponse> {
                override fun onNext(value: DefragmentResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })

    override fun statusMember(): StatusResponse = blockingStub.status(StatusRequest.getDefaultInstance())

    override fun statusMemberInFuture(): ListenableFuture<StatusResponse> =
            futureStub.status(StatusRequest.getDefaultInstance())

    override fun statusMemberAsync(callback: ResponseCallback<StatusResponse>) {
        asyncStub.status(StatusRequest.getDefaultInstance(), object : StreamObserver<StatusResponse> {
            override fun onNext(value: StatusResponse?) {
                value?.let { callback.onResponse(it) }
            }

            override fun onError(t: Throwable?) {
                t?.let { callback.onError(it) }
            }

            override fun onCompleted() = callback.completeCallback()
        })
    }
}