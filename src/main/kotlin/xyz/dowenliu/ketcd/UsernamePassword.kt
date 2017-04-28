package xyz.dowenliu.ketcd

import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.protobuf.toByteString

/**
 * Username&password info
 *
 * create at 2017/4/8
 * @author liufl
 * @since 0.1.0
 *
 * @property username username of [ByteString] format.
 * @property password password of [ByteString] format.
 */
data class UsernamePassword constructor(val username: ByteString, val password: ByteString){
    companion object {
        /**
         * Create an [UsernamePassword] with [String] format username and password.
         *
         * @param username username in [String] format.
         * @param password password in [String] format.
         * @return The [UsernamePassword] created.
         */
        fun of(username: String, password: String): UsernamePassword {
            require(username.isNotBlank(), { "username can not be empty."})
            require(password.isNotBlank(), { "password can not be empty" })
            return UsernamePassword(username.trim().toByteString(), password.trim().toByteString())
        }
    }
}