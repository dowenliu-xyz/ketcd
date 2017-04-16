package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.kv.Txn
import xyz.dowenliu.ketcd.kv.option.CompactOption
import xyz.dowenliu.ketcd.kv.option.DeleteOption
import xyz.dowenliu.ketcd.kv.option.GetOption
import xyz.dowenliu.ketcd.kv.option.PutOption

/**
 * Interface of kv service talking to etcd.
 *
 * create at 2017/4/16
 * @author liufl
 * @since 0.1.0
 */
interface EtcdKVService : AutoCloseable {
    /**
     * Puts the given key into the key-value store (blocking).
     * A put request increments the revision of the key-value store and generates one event in the event history.
     *
     * @param key key of the key/value pair to store.
     * @param value value of the key/value pair to store.
     * @param options put request options.
     * @return [PutResponse]
     */
    fun put(key: String, value: String, options: PutOption = PutOption.DEFAULT): PutResponse
    /**
     * Puts the given key into the key-value store in future.
     * A put request increments the revision of the key-value store and generates one event in the event history.
     *
     * @param key key of the key/value pair to store.
     * @param value value of the key/value pair to store.
     * @param options put request options.
     * @return [ListenableFuture] of [PutResponse]
     */
    fun putInFuture(key: String, value: String, options: PutOption = PutOption.DEFAULT): ListenableFuture<PutResponse>
    /**
     * Puts the given key into the key-value store (asynchronously).
     * A put request increments the revision of the key-value store and generates one event in the event history.
     *
     * @param key key of the key/value pair to store.
     * @param value value of the key/value pair to store.
     * @param options put request options.
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun putAsync(key: String, value: String, options: PutOption = PutOption.DEFAULT, callback: ResponseCallback<PutResponse>)

    /**
     * Gets the key/value pair of key or the key/value pairs in the range from the key-value store (blocking).
     *
     * @param key the key, or the range start, or the prefix.
     * If it's '\u0000' and _endKey_ of the [options] is also '\u0000', all keys will be return
     * (but limited by _limit_ option).
     * @param options get request options
     * @return [RangeResponse]
     */
    fun get(key: String, options: GetOption = GetOption.DEFAULT): RangeResponse

    /**
     * Gets the key/value pair of key or the key/value pairs in the range from the key-value store in future.
     *
     * @param key the key, or the range start, or the prefix.
     * If it's '\u0000' and _endKey_ of the [options] is also '\u0000', all keys will be return
     * (but limited by _limit_ option).
     * @param options get request options
     * @return [ListenableFuture] of [RangeResponse]
     */
    fun getInFuture(key: String, options: GetOption = GetOption.DEFAULT): ListenableFuture<RangeResponse>

    /**
     * Gets the key/value pair of key or the key/value pairs in the range from the key-value store (asynchronously).
     *
     * @param key the key, or the range start, or the prefix.
     * If it's '\u0000' and _endKey_ of the [options] is also '\u0000', all keys will be return
     * (but limited by _limit_ option).
     * @param options get request options
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun getAsync(key: String, options: GetOption = GetOption.DEFAULT, callback: ResponseCallback<RangeResponse>)

    /**
     * Deletes one or the given range from the key-value store (blocking).
     * A delete request increments the revision of the key-value store and
     * generates a delete event in the event history for every deleted key.
     *
     * @param key the key, or the range start, or the prefix.
     * If it's '\u0000' and _endKey_ of the [options] is also '\u0000', all keys will be deleted.
     * @param options delete request options
     * @return [DeleteRangeResponse]
     */
    fun delete(key: String, options: DeleteOption = DeleteOption.DEFAULT): DeleteRangeResponse

    /**
     * Deletes one or the given range from the key-value store in future.
     * A delete request increments the revision of the key-value store and
     * generates a delete event in the event history for every deleted key.
     *
     * @param key the key, or the range start, or the prefix.
     * If it's '\u0000' and _endKey_ of the [options] is also '\u0000', all keys will be deleted.
     * @param options delete request options
     * @return [ListenableFuture] of [DeleteRangeResponse]
     */
    fun deleteInFuture(key: String, options: DeleteOption = DeleteOption.DEFAULT): ListenableFuture<DeleteRangeResponse>

    /**
     * Deletes one or the given range from the key-value store (asynchronously).
     * A delete request increments the revision of the key-value store and
     * generates a delete event in the event history for every deleted key.
     *
     * @param key the key, or the range start, or the prefix.
     * If it's '\u0000' and _endKey_ of the [options] is also '\u0000', all keys will be deleted.
     * @param options delete request options
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun deleteAsync(key: String, options: DeleteOption = DeleteOption.DEFAULT, callback: ResponseCallback<DeleteRangeResponse>)

    /**
     * Compacts the event history in the etcd key-value store (blocking).
     * The key-value store should be periodically compacted or the event history will continue to grow indefinitely.
     *
     * Notes that: you CAN NOT compact at the same revision more tha once. Take care when you call this function
     * without options, as it will always compact at revision 0.
     *
     * @param options compact request options.
     * @return [CompactionResponse]
     */
    fun compact(options: CompactOption = CompactOption.DEFAULT): CompactionResponse

    /**
     * Compacts the event history in the etcd key-value store in future.
     * The key-value store should be periodically compacted or the event history will continue to grow indefinitely.
     *
     * Notes that: you CAN NOT compact at the same revision more tha once. Take care when you call this function
     * without options, as it will always compact at revision 0.
     *
     * @param options compact request options.
     * @return [ListenableFuture] of [CompactionResponse]
     */
    fun compactInFuture(options: CompactOption = CompactOption.DEFAULT): ListenableFuture<CompactionResponse>

    /**
     * Compacts the event history in the etcd key-value store (asynchronously).
     * The key-value store should be periodically compacted or the event history will continue to grow indefinitely.
     *
     * Notes that: you CAN NOT compact at the same revision more tha once. Take care when you call this function
     * without options, as it will always compact at revision 0.
     *
     * @param options compact request options.
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun compactAsync(options: CompactOption = CompactOption.DEFAULT, callback: ResponseCallback<CompactionResponse>)

    /**
     * Processes multiple requests in a single transaction (blocking).
     * A txn request increments the revision of the key-value store and
     * generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     *
     * @param txn Predicate that create a [TxnRequest]
     * @return [TxnResponse]
     */
    fun commit(txn: Txn): TxnResponse

    /**
     * Processes multiple requests in a single transaction in future.
     * A txn request increments the revision of the key-value store and
     * generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     *
     * @param txn Predicate that create a [TxnRequest]
     * @return [ListenableFuture] of [TxnResponse]
     */
    fun commitInFuture(txn: Txn): ListenableFuture<TxnResponse>

    /**
     * Processes multiple requests in a single transaction (asynchronously).
     * A txn request increments the revision of the key-value store and
     * generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     *
     * @param txn Predicate that create a [TxnRequest]
     * @param callback A [ResponseCallback] to handle the response.
     */
    fun commit(txn: Txn, callback: ResponseCallback<TxnResponse>)
}