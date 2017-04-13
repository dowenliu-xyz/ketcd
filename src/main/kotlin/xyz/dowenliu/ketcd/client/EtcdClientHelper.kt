@file:JvmName("EtcdClientHelper")

package xyz.dowenliu.ketcd.client

import io.grpc.ManagedChannelBuilder
import io.grpc.NameResolver
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.resolver.SimpleEtcdNameResolverFactory
import java.net.URI
import java.util.stream.Collectors

/**
 * Help build etcd clients
 *
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 */

internal fun defaultChannelBuilder(factory: NameResolver.Factory): ManagedChannelBuilder<*> =
        ManagedChannelBuilder.forTarget("etcd").nameResolverFactory(factory).usePlaintext(true)

internal fun simpleNameResolverFactory(endpoints: List<Endpoint>): NameResolver.Factory =
        SimpleEtcdNameResolverFactory(endpoints.stream().map { URI(it.toString()) }.collect(Collectors.toList()))
