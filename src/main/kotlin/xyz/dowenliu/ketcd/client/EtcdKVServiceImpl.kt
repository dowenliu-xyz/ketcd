package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.kv.Txn
import xyz.dowenliu.ketcd.kv.option.CompactOption
import xyz.dowenliu.ketcd.kv.option.DeleteOption
import xyz.dowenliu.ketcd.kv.option.GetOption
import xyz.dowenliu.ketcd.kv.option.PutOption

/**
 * Implementation of etcd kv service.
 *
 * create at 2017/4/16
 * @author liufl
 * @since 0.1.0
 */
class EtcdKVServiceImpl internal constructor(val channel: ManagedChannel, val token: String?) : EtcdKVService {
    private val blockingStub = configureStub(KVGrpc.newBlockingStub(channel), token)
    private val futureStub = configureStub(KVGrpc.newFutureStub(channel), token)
    private val asyncStub = configureStub(KVGrpc.newStub(channel), token)

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
            asyncStub.put(putRequest(key, value, options), object : StreamObserver<PutResponse> {
                override fun onNext(value: PutResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })

    private fun getRequest(key: ByteString, options: GetOption): RangeRequest {
        val builder = RangeRequest.newBuilder()
                .setKey(key)
                .setCountOnly(options.countOnly)
                .setLimit(options.limit)
                .setRevision(options.revision)
                .setKeysOnly(options.keysOnly)
                .setSerializable(options.serializable)
                .setSortOrder(options.sortOrder)
                .setSortTarget(options.sortTarget)
        options.endKey?.let { builder.rangeEnd = it }
        return builder.build()
    }

    override fun get(key: ByteString, options: GetOption): RangeResponse = blockingStub.range(getRequest(key, options))

    override fun getInFuture(key: ByteString, options: GetOption): ListenableFuture<RangeResponse> =
            futureStub.range(getRequest(key, options))

    override fun getAsync(key: ByteString, options: GetOption, callback: ResponseCallback<RangeResponse>) =
            asyncStub.range(getRequest(key, options), object : StreamObserver<RangeResponse> {
                override fun onNext(value: RangeResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })

    private fun deleteRequest(key: ByteString, options: DeleteOption): DeleteRangeRequest {
        val builder = DeleteRangeRequest.newBuilder()
                .setKey(key)
                .setPrevKv(options.prevKV)
        options.endKey?.let { builder.rangeEnd = it }
        return builder.build()
    }

    override fun delete(key: ByteString, options: DeleteOption): DeleteRangeResponse =
            blockingStub.deleteRange(deleteRequest(key, options))

    override fun deleteInFuture(key: ByteString, options: DeleteOption): ListenableFuture<DeleteRangeResponse> =
            futureStub.deleteRange(deleteRequest(key, options))

    override fun deleteAsync(key: ByteString, options: DeleteOption, callback: ResponseCallback<DeleteRangeResponse>) =
            asyncStub.deleteRange(deleteRequest(key, options), object : StreamObserver<DeleteRangeResponse> {
                override fun onNext(value: DeleteRangeResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })

    private fun compactRequest(options: CompactOption): CompactionRequest =
            CompactionRequest.newBuilder()
                    .setRevision(options.revision)
                    .setPhysical(options.physical)
                    .build()

    override fun compact(options: CompactOption): CompactionResponse = blockingStub.compact(compactRequest(options))

    override fun compactInFuture(options: CompactOption): ListenableFuture<CompactionResponse> =
            futureStub.compact(compactRequest(options))

    override fun compactAsync(options: CompactOption, callback: ResponseCallback<CompactionResponse>) =
            asyncStub.compact(compactRequest(options), object : StreamObserver<CompactionResponse> {
                override fun onNext(value: CompactionResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })

    override fun commit(txn: Txn): TxnResponse = blockingStub.txn(txn.toTxnRequest())

    override fun commitInFuture(txn: Txn): ListenableFuture<TxnResponse> = futureStub.txn(txn.toTxnRequest())

    override fun commitAsync(txn: Txn, callback: ResponseCallback<TxnResponse>) =
            asyncStub.txn(txn.toTxnRequest(), object : StreamObserver<TxnResponse> {
                override fun onNext(value: TxnResponse?) {
                    value?.let { callback.onResponse(it) }
                }

                override fun onError(t: Throwable?) {
                    t?.let { callback.onError(it) }
                }

                override fun onCompleted() = callback.completeCallback()
            })
}