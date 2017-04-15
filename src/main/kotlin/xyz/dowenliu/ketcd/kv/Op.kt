package xyz.dowenliu.ketcd.kv

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.api.DeleteRangeRequest
import xyz.dowenliu.ketcd.api.PutRequest
import xyz.dowenliu.ketcd.api.RangeRequest
import xyz.dowenliu.ketcd.api.RequestOp
import xyz.dowenliu.ketcd.kv.option.DeleteOption
import xyz.dowenliu.ketcd.kv.option.GetOption
import xyz.dowenliu.ketcd.kv.option.PutOption

/**
 * Etcd Operation
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 */
abstract class Op internal constructor(protected val type: Type, protected val key: ByteString) {
    companion object {
        @JvmStatic fun put(key: ByteString, value: ByteString, option: PutOption = PutOption.DEFAULT): PutOp =
                PutOp(key, value, option)

        @JvmStatic fun get(key: ByteString, option: GetOption = GetOption.DEFAULT): GetOp = GetOp(key, option)

        @JvmStatic fun delete(key: ByteString, option: DeleteOption = DeleteOption.DEFAULT): DeleteOp = DeleteOp(key, option)
    }

    abstract fun toRequestOp(): RequestOp

    /**
     * Op type.
     */
    enum class Type {
        PUT, RANGE, DELETE_RANGE
    }

    class PutOp internal constructor(key: ByteString,
                                     private val value: ByteString,
                                     private val option: PutOption) : Op(Type.PUT, key) {
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

    class GetOp internal constructor(key: ByteString, private val option: GetOption) : Op(Type.RANGE, key) {
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
            option.endKey?.let { builder.rangeEnd = it }
            return RequestOp.newBuilder().setRequestRange(builder).build()
        }
    }

    class DeleteOp internal constructor(key: ByteString, private val option: DeleteOption) : Op(Type.DELETE_RANGE, key) {
        override fun toRequestOp(): RequestOp {
            val builder = DeleteRangeRequest.newBuilder()
                    .setKey(key)
                    .setPrevKv(option.prevKV)
            option.endKey?.let { builder.rangeEnd = it }
            return RequestOp.newBuilder().setRequestDeleteRange(builder).build()
        }
    }
}