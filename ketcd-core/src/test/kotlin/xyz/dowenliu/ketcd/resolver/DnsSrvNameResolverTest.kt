package xyz.dowenliu.ketcd.resolver

import io.grpc.Attributes
import io.grpc.internal.GrpcUtil
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import java.net.URI

/**
 * create at 2017/4/9
 * @author liufl
 * @since 0.1.0
 */
class DnsSrvNameResolverTest {
    private val test = Assertion()

    @Test
    fun testResolver() {
        val discovery = DnsSrvNameResolver("_xmpp-server._tcp.gmail.com", GrpcUtil.SHARED_CHANNEL_EXECUTOR)
        test.assertFalse(discovery.getServers().isEmpty())
    }

    @Test
    fun testResolverFactory() {
        test.assertEquals((DnsSrvNameResolverFactory().newNameResolver(URI.create("dns+srv:///my-domain-1.com"), Attributes.EMPTY) as DnsSrvNameResolver).name, "_etcd-server._tcp.my-domain-1.com")
        test.assertEquals((DnsSrvNameResolverFactory().newNameResolver(URI.create("dns+srv:///_etcd-server._tcp.my-domain-2.com"), Attributes.EMPTY) as DnsSrvNameResolver).name, "_etcd-server._tcp.my-domain-2.com")
    }
}