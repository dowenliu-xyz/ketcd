package xyz.dowenliu.ketcd.resolver

import io.grpc.Attributes
import io.grpc.ResolvedServerInfo
import io.grpc.internal.SharedResourceHolder
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.stream.Collectors

/**
 * SimpleEtcdNameResolver returns pre-configured addresses to the caller.
 *
 * create at 2017/4/8
 * @author liufl
 * @since 3.1.0
 */
class SimpleEtcdNameResolver(name: String,
                             executorResource: SharedResourceHolder.Resource<ExecutorService>,
                             uris: List<URI>) : AbstractEtcdNameResolver(name, executorResource) {
    private val _servers: List<ResolvedServerInfo> = uris.stream().map {
        ResolvedServerInfo(InetSocketAddress(it.host, it.port), Attributes.EMPTY)
    }.collect(Collectors.toList())

    override fun getServers(): List<ResolvedServerInfo> = _servers
}