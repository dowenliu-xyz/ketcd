package xyz.dowenliu.ketcd.client

import io.grpc.stub.StreamObserver

/**
 * create at 2017/4/17
 * @author liufl
 * @since 0.1.0
 */
internal class CallbackStreamObserver<T> constructor(private val callback: ResponseCallback<T>) : StreamObserver<T> {
    override fun onNext(value: T?) {
        value?.let { callback.onResponse(it) }
    }

    override fun onError(t: Throwable?) {
        t?.let { callback.onError(it) }
    }

    override fun onCompleted() = callback.completeCallback()
}