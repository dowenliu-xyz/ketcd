package xyz.dowenliu.ketcd.protobuf

import com.google.protobuf.ByteString
import org.testng.annotations.Test
import org.testng.asserts.Assertion

/**
 * create at 2017/6/13
 * @author liufl
 * @since 0.1.2
 */
class ByteStringHelperTest {
    private val test: Assertion = Assertion()

    @Test
    fun testByteToUnsignedByte() {
        val intFF = 0xff
        test.assertTrue(intFF > Byte.MAX_VALUE)
        val byte = intFF.toByte()
        test.assertNotEquals(byte.toInt(), intFF)
        test.assertEquals(byte.toUnsignedByte(), intFF)
    }

    @Test
    fun testByteStringOneBitLargerOf() {
        val aa =  "aa".toByteString()
        val ab = "ab"
        val aff = "a".toByteString()
                .toByteArray()
                .toMutableList().let { it.add(0xff.toByte()); it }
                .toByteArray()
                .let { ByteString.copyFrom(it) }
        val b = "b"
        test.assertEquals(ab, aa.oneBitLargerOf().toStringUtf8())
        test.assertEquals(b, aff.oneBitLargerOf().toStringUtf8())
    }
}