package xyz.dowenliu.ketcd.resolver

import io.grpc.NameResolver

/**
 * The abstract etcd name resolver factory. Name resolver factory is responsible for
 * creating specific name resolver which provides addresses for [io.grpc.LoadBalancer].
 *
 * create at 2017/4/8
 * @author liufl
 * @since 3.1.0
 */
abstract class AbstractEtcdNameResolverFactory : NameResolver.Factory() {
    /**
     * Gets name of name resolver
     */
    abstract fun name(): String
}
