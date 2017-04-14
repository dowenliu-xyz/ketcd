package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.MemberAddResponse
import xyz.dowenliu.ketcd.api.MemberListResponse
import xyz.dowenliu.ketcd.api.MemberRemoveResponse
import xyz.dowenliu.ketcd.api.MemberUpdateResponse

/**
 * Interface of cluster service talking to etcd
 *
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
interface EtcdClusterService {
    /**
     * Lists the current cluster membership (blocking).
     *
     * @return [MemberListResponse]
     */
    fun listMember(): MemberListResponse

    /**
     * Lists the current cluster membership as future.
     *
     * @return [ListenableFuture] of [MemberListResponse]
     */
    fun listMemberInFuture(): ListenableFuture<MemberListResponse>

    /**
     * Lists the current cluster membership (asynchronously).
     *
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun listMemberAsync(callback: ResponseCallback<MemberListResponse>)

    /**
     * Add a new member into the cluster (blocking).
     *
     * @param peerAddresses The **peer** address of the new member.
     * @return [MemberAddResponse]
     */
    fun addMember(peerAddresses: Array<Endpoint>): MemberAddResponse

    /**
     * Add a new member into the cluster as future.
     *
     * @param peerAddresses The **peer** address of the new member.
     * @return [ListenableFuture] of [MemberAddResponse]
     */
    fun addMemberInFuture(peerAddresses: Array<Endpoint>): ListenableFuture<MemberAddResponse>

    /**
     * Add a new member into the cluster (asynchronously).
     *
     * @param peerAddresses The **peer** address of the new member.
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun addMemberAsync(peerAddresses: Array<Endpoint>, callback: ResponseCallback<MemberAddResponse>)

    /**
     * Remove an existing member from the cluster (blocking).
     *
     * @param memberId The id of the member to remove.
     * @return [MemberRemoveResponse]
     */
    fun removeMember(memberId: Long): MemberRemoveResponse

    /**
     * Remove an existing member from the cluster as future.
     *
     * @param memberId The id of the member to remove.
     * @return [ListenableFuture] of [MemberRemoveResponse]
     */
    fun removeMemberInFuture(memberId: Long): ListenableFuture<MemberRemoveResponse>

    /**
     *  Remove an existing member from the cluster (blocking).
     *
     *  @param memberId The id of the member to remove.
     *  @param callback A [ResponseCallback] to handle the response.
     */
    fun removeMemberAsync(memberId: Long, callback: ResponseCallback<MemberRemoveResponse>)

    /**
     * Update peer address of the member (blocking).
     *
     * @param memberId The id of the member to update.
     * @param peerAddresses The new **peer** address of the member.
     * @return [MemberUpdateResponse]
     */
    fun updateMember(memberId: Long, peerAddresses: Array<Endpoint>): MemberUpdateResponse

    /**
     * Update peer address of the member as future.
     *
     * @param memberId The id of the member to update.
     * @param peerAddresses The new **peer** address of the member.
     * @return [ListenableFuture] of [MemberUpdateResponse]
     */
    fun updateMemberInFuture(memberId: Long, peerAddresses: Array<Endpoint>): ListenableFuture<MemberUpdateResponse>

    /**
     * Update peer address of the member (asynchronously).
     *
     * @param memberId The id of the member to update.
     * @param peerAddresses The new **peer** address of the member.
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun updateMemberAsync(memberId: Long, peerAddresses: Array<Endpoint>, callback: ResponseCallback<MemberUpdateResponse>)
}