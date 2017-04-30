package xyz.dowenliu.ketcd.kv

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.DeleteRangeRequest
import xyz.dowenliu.ketcd.api.PutRequest
import xyz.dowenliu.ketcd.api.RangeRequest
import xyz.dowenliu.ketcd.api.RequestOp
import xyz.dowenliu.ketcd.option.DeleteOption
import xyz.dowenliu.ketcd.option.GetOption
import xyz.dowenliu.ketcd.option.PutOption

/**
 * Etcd Operation
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @property key The start key of the operation target key range.
 */
abstract class Op internal constructor(protected val key: ByteString) {
    /**
     * Companion object of [Op]
     */
    companion object {
        /**
         * Factor a put operation predicate.
         *
         * @param key The start key of the operation target key range.
         * @param value Value to put.
         * @param option Put operation options.
         * @return A [PutOp] to predicate a [RequestOp] containing a PUT request.
         */
        @JvmStatic fun put(key: ByteString, value: ByteString, option: PutOption = PutOption.DEFAULT): PutOp =
                PutOp(key, value, option)

        /**
         * Factor a get operation predicate.
         *
         * @param key The start key of the operation target key range.
         * @param option Get operation options.
         * @return A [GetOp] to predicate a [RequestOp] containing a GET request.
         */
        @JvmStatic fun get(key: ByteString, option: GetOption = GetOption.DEFAULT): GetOp = GetOp(key, option)

        /**
         * Factor a delete operation predicate.
         *
         * @param key The start key of the operation target key range.
         * @param option Delete operation options.
         * @return A [DeleteOp] to predicate a [RequestOp] containing a DELETE request.
         */
        @JvmStatic fun delete(key: ByteString, option: DeleteOption = DeleteOption.DEFAULT): DeleteOp = DeleteOp(key, option)
    }

    /**
     * Predicate a [RequestOp] containing one request.
     *
     * @return A [RequestOp] containing one request.
     */
    abstract fun toRequestOp(): RequestOp

    /**
     * A [PutOp] to predicate a [RequestOp] containing a PUT request.
     *
     * @param key The start key of the operation target key range.
     * @property value Value to put.
     * @property option Put operation options.
     */
    class PutOp internal constructor(key: ByteString,
                                     private val value: ByteString,
                                     private val option: PutOption) : Op(key) {
        override fun toRequestOp(): RequestOp {
            val request = PutRequest.newBuilder()
                    .setKey(key)
                    .setValue(value)
                    .setLease(option.leaseId)
                    .setPrevKv(option.prevKV)
                    .build()
            return RequestOp.newBuilder().setRequestPut(request).build()
        }
    }

    /**
     * A [GetOp] to predicate a [RequestOp] containing a GET request.
     *
     * @param key The start key of the operation target key range.
     * @property option Get operation options.
     */
    class GetOp internal constructor(key: ByteString, private val option: GetOption) : Op(key) {
        override fun toRequestOp(): RequestOp {
            val builder = RangeRequest.newBuilder()
                    .setKey(key)
                    .setCountOnly(option.countOnly)
                    .setLimit(option.limit)
                    .setRevision(option.revision)
                    .setKeysOnly(option.keysOnly)
                    .setSerializable(option.serializable)
                    .setSortOrder(option.sortOrder)
                    .setSortTarget(option.sortTarget)
                    .setRangeEnd(option.endKey)
            return RequestOp.newBuilder().setRequestRange(builder).build()
        }
    }

    /**
     * A [DeleteOp] to predicate a [RequestOp] containing a DELETE request.
     *
     * @param key The start key of the operation target key range.
     * @property option Delete operation options.
     */
    class DeleteOp internal constructor(key: ByteString, private val option: DeleteOption) : Op(key) {
        override fun toRequestOp(): RequestOp {
            val builder = DeleteRangeRequest.newBuilder()
                    .setKey(key)
                    .setPrevKv(option.prevKV)
                    .setRangeEnd(option.endKey)
            return RequestOp.newBuilder().setRequestDeleteRange(builder).build()
        }
    }
}