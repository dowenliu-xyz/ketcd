package xyz.dowenliu.ketcd.client

/**
 * Callback handling received response.
 *
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
interface ResponseCallback<in T> {
    /**
     * Handle response received.
     *
     * @param response The response received.
     */
    fun onResponse(response: T)

    /**
     * When exception caught
     */
    fun onError(throwable: Throwable)

    /**
     * To complete this callback
     */
    fun completeCallback()
}