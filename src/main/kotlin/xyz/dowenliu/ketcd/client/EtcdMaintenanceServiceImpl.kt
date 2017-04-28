package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannel
import xyz.dowenliu.ketcd.api.*

/**
 * Implementation of maintenance service.
 *
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
class EtcdMaintenanceServiceImpl internal constructor(override val client: EtcdClient) : EtcdMaintenanceService {
    private val channel: ManagedChannel = client.channelBuilder.build()
    private val blockingStub = configureStub(MaintenanceGrpc.newBlockingStub(channel), client.token)
    private val futureStub = configureStub(MaintenanceGrpc.newFutureStub(channel), client.token)
    private val asyncStub = configureStub(MaintenanceGrpc.newStub(channel), client.token)

    /**
     * Close this service instance.
     */
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
            asyncStub.alarm(listAlarmsRequest(), CallbackStreamObserver(callback))

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
            asyncStub.alarm(deactiveAlarmRequest(member), CallbackStreamObserver(callback))

    override fun defragmentMember(): DefragmentResponse =
            blockingStub.defragment(DefragmentRequest.getDefaultInstance())

    override fun defragmentMemberInFuture(): ListenableFuture<DefragmentResponse> =
            futureStub.defragment(DefragmentRequest.getDefaultInstance())

    override fun defragmentMemberAsync(callback: ResponseCallback<DefragmentResponse>) =
            asyncStub.defragment(DefragmentRequest.getDefaultInstance(), CallbackStreamObserver(callback))

    override fun statusMember(): StatusResponse = blockingStub.status(StatusRequest.getDefaultInstance())

    override fun statusMemberInFuture(): ListenableFuture<StatusResponse> =
            futureStub.status(StatusRequest.getDefaultInstance())

    override fun statusMemberAsync(callback: ResponseCallback<StatusResponse>) {
        asyncStub.status(StatusRequest.getDefaultInstance(), CallbackStreamObserver(callback))
    }
}