@file:JvmName("ByteStringHelper")

package xyz.dowenliu.ketcd.protobuf

import com.google.protobuf.ByteString

/**
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 */

fun String.toByteString(): ByteString = ByteString.copyFromUtf8(this)

fun ByteString.oneBitLarger(): ByteString {
    val byteArray = this.toByteArray()
    byteArray[byteArray.size - 1] = byteArray.last().inc()
    return ByteString.copyFrom(byteArray)
}