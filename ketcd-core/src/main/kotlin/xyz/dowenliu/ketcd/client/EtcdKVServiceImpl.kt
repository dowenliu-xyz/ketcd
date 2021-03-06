package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.kv.Txn
import xyz.dowenliu.ketcd.option.CompactOption
import xyz.dowenliu.ketcd.option.DeleteOption
import xyz.dowenliu.ketcd.option.GetOption
import xyz.dowenliu.ketcd.option.PutOption

/**
 * Implementation of etcd kv service.
 *
 * create at 2017/4/16
 * @author liufl
 * @since 0.1.0
 */
class EtcdKVServiceImpl internal constructor(override val client: EtcdClient) : EtcdKVService {
    private val channel: ManagedChannel = client.channelBuilder.build()
    private val blockingStub = configureStub(KVGrpc.newBlockingStub(channel), client.token)
    private val futureStub = configureStub(KVGrpc.newFutureStub(channel), client.token)
    private val asyncStub = configureStub(KVGrpc.newStub(channel), client.token)

    /**
     * Close this service instance.
     */
    override fun close() {
        channel.shutdownNow()
    }

    private fun putRequest(key: ByteString, value: ByteString, options: PutOption): PutRequest =
            PutRequest.newBuilder()
                    .setKey(key)
                    .setValue(value)
                    .setLease(options.leaseId)
                    .setPrevKv(options.prevKV)
                    .build()

    override fun put(key: ByteString, value: ByteString, options: PutOption): PutResponse =
            blockingStub.put(putRequest(key, value, options))

    override fun putInFuture(key: ByteString, value: ByteString, options: PutOption): ListenableFuture<PutResponse> =
            futureStub.put(putRequest(key, value, options))

    override fun putAsync(key: ByteString, value: ByteString, options: PutOption, callback: ResponseCallback<PutResponse>) =
            asyncStub.put(putRequest(key, value, options), CallbackStreamObserver(callback))

    private fun getRequest(key: ByteString, options: GetOption): RangeRequest =
            RangeRequest.newBuilder()
                    .setKey(key)
                    .setCountOnly(options.countOnly)
                    .setLimit(options.limit)
                    .setRevision(options.revision)
                    .setKeysOnly(options.keysOnly)
                    .setSerializable(options.serializable)
                    .setSortOrder(options.sortOrder)
                    .setSortTarget(options.sortTarget)
                    .setRangeEnd(options.endKey)
                    .build()

    override fun get(key: ByteString, options: GetOption): RangeResponse = blockingStub.range(getRequest(key, options))

    override fun getInFuture(key: ByteString, options: GetOption): ListenableFuture<RangeResponse> =
            futureStub.range(getRequest(key, options))

    override fun getAsync(key: ByteString, options: GetOption, callback: ResponseCallback<RangeResponse>) =
            asyncStub.range(getRequest(key, options), CallbackStreamObserver(callback))

    private fun deleteRequest(key: ByteString, options: DeleteOption): DeleteRangeRequest =
            DeleteRangeRequest.newBuilder()
                    .setKey(key)
                    .setPrevKv(options.prevKV)
                    .setRangeEnd(options.endKey)
                    .build()

    override fun delete(key: ByteString, options: DeleteOption): DeleteRangeResponse =
            blockingStub.deleteRange(deleteRequest(key, options))

    override fun deleteInFuture(key: ByteString, options: DeleteOption): ListenableFuture<DeleteRangeResponse> =
            futureStub.deleteRange(deleteRequest(key, options))

    override fun deleteAsync(key: ByteString, options: DeleteOption, callback: ResponseCallback<DeleteRangeResponse>) =
            asyncStub.deleteRange(deleteRequest(key, options), CallbackStreamObserver(callback))

    private fun compactRequest(options: CompactOption): CompactionRequest =
            CompactionRequest.newBuilder()
                    .setRevision(options.revision)
                    .setPhysical(options.physical)
                    .build()

    override fun compact(options: CompactOption): CompactionResponse = blockingStub.compact(compactRequest(options))

    override fun compactInFuture(options: CompactOption): ListenableFuture<CompactionResponse> =
            futureStub.compact(compactRequest(options))

    override fun compactAsync(options: CompactOption, callback: ResponseCallback<CompactionResponse>) =
            asyncStub.compact(compactRequest(options), CallbackStreamObserver(callback))

    override fun commit(txn: Txn): TxnResponse = blockingStub.txn(txn.toTxnRequest())

    override fun commitInFuture(txn: Txn): ListenableFuture<TxnResponse> = futureStub.txn(txn.toTxnRequest())

    override fun commitAsync(txn: Txn, callback: ResponseCallback<TxnResponse>) =
            asyncStub.txn(txn.toTxnRequest(), CallbackStreamObserver(callback))
}