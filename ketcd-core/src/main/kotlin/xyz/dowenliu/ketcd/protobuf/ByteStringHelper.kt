@file:JvmName("ByteStringHelper")

package xyz.dowenliu.ketcd.protobuf

import com.google.protobuf.ByteString

/**
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 */

/**
 * Extension function for [String] to build [ByteString].
 *
 * @receiver [String]
 * @return The [ByteString] build from the [String] with [ByteString.copyFromUtf8]
 */
fun String.toByteString(): ByteString = ByteString.copyFromUtf8(this)

/**
 * Extension function for [ByteString] to build another [ByteString] which is
 * [one big larger](https://github.com/coreos/etcd/blob/v3.1.6/etcdserver/etcdserverpb/rpc.proto#L355)
 *
 * @receiver [ByteString]
 * @return Another [ByteString] witch is 'one bit larger'.
 */
fun ByteString.oneBitLarger(): ByteString {
    val byteArray = this.toByteArray()
    byteArray[byteArray.size - 1] = byteArray.last().inc()
    return ByteString.copyFrom(byteArray)
}