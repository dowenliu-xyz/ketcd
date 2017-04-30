@file:JvmName("EtcdClientHelper")

package xyz.dowenliu.ketcd.client

import io.grpc.ManagedChannelBuilder
import io.grpc.NameResolver
import io.grpc.stub.AbstractStub
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.resolver.SimpleEtcdNameResolverFactory
import java.net.URI
import java.util.stream.Collectors

/**
 * Help build etcd client
 *
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 */

internal fun defaultChannelBuilder(factory: NameResolver.Factory): ManagedChannelBuilder<*> =
        ManagedChannelBuilder.forTarget("etcd").nameResolverFactory(factory).usePlaintext(true)

internal fun simpleNameResolverFactory(endpoints: List<Endpoint>): NameResolver.Factory =
        SimpleEtcdNameResolverFactory(endpoints.stream().map { URI(it.toString()) }.collect(Collectors.toList()))

internal fun <T: AbstractStub<T>> configureStub(stub: T, token: String?): T {
    token ?: return stub
    val metadata = io.grpc.Metadata()
    metadata.put(io.grpc.Metadata.Key.of("token", io.grpc.Metadata.ASCII_STRING_MARSHALLER), token)
    return stub.withCallCredentials { _, _, _, applier -> applier.apply(metadata) }
}
