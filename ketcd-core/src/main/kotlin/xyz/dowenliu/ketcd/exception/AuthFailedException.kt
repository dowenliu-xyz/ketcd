package xyz.dowenliu.ketcd.exception

/**
 * Signals that an error occurred while attempting to auth, this may caused by wrong username or password
 *
 * create at 2017/4/9
 * @author liufl
 * @since 0.1.0
 */
class AuthFailedException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)