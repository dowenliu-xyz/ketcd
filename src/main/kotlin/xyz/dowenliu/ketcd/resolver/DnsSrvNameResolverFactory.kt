package xyz.dowenliu.ketcd.resolver

import io.grpc.Attributes
import io.grpc.NameResolver
import io.grpc.internal.GrpcUtil
import java.net.URI

/**
 * A custom name resolver factory which creates etcd name resolver
 *
 * create at 2017/4/9
 * @author liufl
 * @since 0.1.0
 */
class DnsSrvNameResolverFactory : AbstractEtcdNameResolverFactory() {
    companion object {
        private const val SCHEME = "dns+srv"
        private const val NAME = "dns+srv"
    }

    override fun newNameResolver(targetUri: URI?, params: Attributes?): NameResolver? {
        if (targetUri == null) return null
        if (SCHEME != targetUri.scheme) return null
        val targetPath = requireNotNull(targetUri.path, { "targetPath is null." })
        require(targetPath.startsWith('/'),
                { "the path component ($targetPath) of the target ($targetUri) must start with '/'" })
        var name = targetPath.substring(1)
        if (!name.startsWith("_etcd-server._tcp.")) name = "_etcd-server._tcp.$name"
        return DnsSrvNameResolver(name, GrpcUtil.SHARED_CHANNEL_EXECUTOR)
    }

    override fun getDefaultScheme(): String = SCHEME

    override fun name(): String = NAME
}