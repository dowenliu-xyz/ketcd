@file:JvmName("KeyHelper")

package xyz.dowenliu.ketcd.kv

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.protobuf.oneBitLarger
import xyz.dowenliu.ketcd.protobuf.toByteString

/**
 * create at 2017/4/17
 * @author liufl
 * @since 0.1.0
 */

/**
 * EndKey option value for get/delete range all keys >= key (--from-key).
 */
val FROM_KEY: ByteString = ByteString.copyFrom("\u0000", Charsets.UTF_8)

val NULL_KEY: ByteString = FROM_KEY

/**
 * EndKey option value for get/delete prefixed by [string] (--prefix)
 *
 * @param string The prefix string.
 */
fun prefixKeyOf(string: ByteString): ByteString = string.oneBitLarger()

/**
 * EndKey option value for get/delete prefixed by [string] (--prefix)
 *
 * @param string The prefix string.
 */
fun prefixKeyOf(string: String): ByteString = prefixKeyOf(string.toByteString())