package xyz.dowenliu.ketcd.exception

/**
 * Signals that an error occurred while attempting to connect to etcd.
 *
 * create at 2017/4/9
 * @author liufl
 * @since 0.1.0
 */
class ConnectException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)