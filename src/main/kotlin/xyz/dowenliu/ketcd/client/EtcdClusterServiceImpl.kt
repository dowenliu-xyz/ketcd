package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.*
import java.util.*
import java.util.stream.Collectors

/**
 * Implementation of cluster service.
 *
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
class EtcdClusterServiceImpl internal constructor(val channel: ManagedChannel, val token: String?) :
        EtcdClusterService {
    private val blockingStub = configureStub(ClusterGrpc.newBlockingStub(channel), token)
    private val futureStub = configureStub(ClusterGrpc.newFutureStub(channel), token)
    private val asyncStub = configureStub(ClusterGrpc.newStub(channel), token)

    override fun close() {
        channel.shutdownNow()
    }

    override fun listMember(): MemberListResponse = blockingStub.memberList(MemberListRequest.getDefaultInstance())

    override fun listMemberInFuture(): ListenableFuture<MemberListResponse> =
            futureStub.memberList(MemberListRequest.getDefaultInstance())

    override fun listMemberAsync(callback: ResponseCallback<MemberListResponse>) =
            asyncStub.memberList(MemberListRequest.getDefaultInstance(), CallbackStreamObserver(callback))

    private fun addMemberRequest(peerAddresses: Array<Endpoint>): MemberAddRequest {
        require(peerAddresses.isNotEmpty(), { "Peer address for a member should not be empty." })
        return MemberAddRequest.newBuilder()
                .addAllPeerURLs(Arrays.stream(peerAddresses).map { it.toString() }.collect(Collectors.toList()))
                .build()
    }

    override fun addMember(peerAddresses: Array<Endpoint>): MemberAddResponse =
            blockingStub.memberAdd(addMemberRequest(peerAddresses))

    override fun addMemberInFuture(peerAddresses: Array<Endpoint>): ListenableFuture<MemberAddResponse> =
            futureStub.memberAdd(addMemberRequest(peerAddresses))

    override fun addMemberAsync(peerAddresses: Array<Endpoint>, callback: ResponseCallback<MemberAddResponse>) =
            asyncStub.memberAdd(addMemberRequest(peerAddresses), CallbackStreamObserver(callback))

    override fun removeMember(memberId: Long): MemberRemoveResponse =
            blockingStub.memberRemove(MemberRemoveRequest.newBuilder().setID(memberId).build())

    override fun removeMemberInFuture(memberId: Long): ListenableFuture<MemberRemoveResponse> =
            futureStub.memberRemove(MemberRemoveRequest.newBuilder().setID(memberId).build())

    override fun removeMemberAsync(memberId: Long, callback: ResponseCallback<MemberRemoveResponse>) =
            asyncStub.memberRemove(MemberRemoveRequest.newBuilder().setID(memberId).build(),
                    CallbackStreamObserver(callback))

    private fun updateMemberRequest(memberId: Long, peerAddresses: Array<Endpoint>): MemberUpdateRequest {
        require(peerAddresses.isNotEmpty(), { "Peer address for a member should not be empty." })
        return MemberUpdateRequest.newBuilder()
                .addAllPeerURLs(Arrays.stream(peerAddresses).map { it.toString() }.collect(Collectors.toList()))
                .setID(memberId)
                .build()
    }

    override fun updateMember(memberId: Long, peerAddresses: Array<Endpoint>): MemberUpdateResponse =
            blockingStub.memberUpdate(updateMemberRequest(memberId, peerAddresses))

    override fun updateMemberInFuture(memberId: Long, peerAddresses: Array<Endpoint>):
            ListenableFuture<MemberUpdateResponse> =
            futureStub.memberUpdate(updateMemberRequest(memberId, peerAddresses))

    override fun updateMemberAsync(memberId: Long, peerAddresses: Array<Endpoint>,
                                   callback: ResponseCallback<MemberUpdateResponse>) =
            asyncStub.memberUpdate(updateMemberRequest(memberId, peerAddresses), CallbackStreamObserver(callback))
}