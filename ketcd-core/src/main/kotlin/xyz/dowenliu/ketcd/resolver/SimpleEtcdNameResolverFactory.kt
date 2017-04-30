package xyz.dowenliu.ketcd.resolver

import io.grpc.Attributes
import io.grpc.NameResolver
import io.grpc.internal.GrpcUtil
import java.net.URI

/**
 * A custom name resolver factory which creates etcd name resolver.
 *
 * create at 2017/4/8
 * @author liufl
 * @since 3.1.0
 *
 * @property uris Pre-configured known Etcd endpoint URIs.
 */
class SimpleEtcdNameResolverFactory(private val uris: List<URI>) : AbstractEtcdNameResolverFactory() {
    /**
     * Companion object of [SimpleEtcdNameResolverFactory]
     */
    companion object {
        private const val SCHEME = "etcd"
        private const val NAME = "simple"
    }

    override fun newNameResolver(targetUri: URI?, params: Attributes?): NameResolver? {
        val _targetUri = requireNotNull(targetUri)
        if (SCHEME != _targetUri.scheme) return null
        val targetPath = checkNotNull(_targetUri.path, { "targetPath is null" })
        check(targetPath.startsWith("/"),
                { "the path component ($targetPath) of the target ($_targetUri) must start with '/'" })
        val name = targetPath.substring(1)
        return SimpleEtcdNameResolver(name, GrpcUtil.SHARED_CHANNEL_EXECUTOR, this.uris)
    }

    override fun getDefaultScheme(): String = SCHEME

    override fun name(): String = NAME
}