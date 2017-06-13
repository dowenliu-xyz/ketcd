@file:JvmName("KeyHelper")

package xyz.dowenliu.ketcd.kv

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.protobuf.oneBitLargerOf
import xyz.dowenliu.ketcd.protobuf.toByteString

/**
 * create at 2017/4/17
 * @author liufl
 * @since 0.1.0
 */

/**
 * EndKey option value for key range: all keys >= key (--from-key).
 *
 * Values: '\u0000'
 */
val FROM_KEY: ByteString = ByteString.copyFrom("\u0000", Charsets.UTF_8)

/**
 * Smallest key for key range. Together with endKey [FROM_KEY] or '\u0000' expresses key range ALL.
 *
 * Values: '\u0000'
 */
val NULL_KEY: ByteString = FROM_KEY

/**
 * EndKey option value for get/delete prefixed by [string] (--prefix)
 *
 * @param string The prefix string.
 */
fun prefixKeyOf(string: ByteString): ByteString = string.oneBitLargerOf()

/**
 * EndKey option value for get/delete prefixed by [string] (--prefix)
 *
 * @param string The prefix string.
 */
fun prefixKeyOf(string: String): ByteString = prefixKeyOf(string.toByteString())