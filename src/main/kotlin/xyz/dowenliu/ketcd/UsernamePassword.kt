package xyz.dowenliu.ketcd

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.protobuf.toByteString

/**
 * Username&password info
 *
 * create at 2017/4/8
 * @author liufl
 * @since 0.1.0
 */
data class UsernamePassword internal constructor(val username: ByteString, val password: ByteString){
    companion object {
        fun of(username: String, password: String): UsernamePassword {
            require(username.isNotBlank(), { "username can not be empty."})
            require(password.isNotBlank(), { "password can not be empty" })
            return UsernamePassword(username.trim().toByteString(), password.trim().toByteString())
        }
    }
}