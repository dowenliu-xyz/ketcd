package xyz.dowenliu.ketcd.resolver

import io.grpc.Attributes
import io.grpc.ResolvedServerInfo
import io.grpc.internal.SharedResourceHolder
import org.jetbrains.annotations.TestOnly
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ExecutorService
import javax.naming.directory.InitialDirContext

/**
 * create at 2017/4/9
 * @author liufl
 * @since 0.1.0
 */
class DnsSrvNameResolver(name: String, executorResource: SharedResourceHolder.Resource<ExecutorService>) :
        AbstractEtcdNameResolver(name, executorResource) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DnsSrvNameResolver::class.java)
        private val ATTRIBUTE_IDS = arrayOf("SRV")
        private val ENV: Hashtable<String, String> = Hashtable(mapOf(
                Pair("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory"),
                Pair("java.naming.provider.url", "dns:")
        ))

        private fun srvRecordToAddress(dnsSrvRecord: String): InetSocketAddress {
            val split = dnsSrvRecord.split(" ")
            return InetSocketAddress(split[3].trim(), split[2].trim().toInt())
        }

        private fun srvRecordToServerInfo(dnsSrvRecord: String): ResolvedServerInfo =
                ResolvedServerInfo(srvRecordToAddress(dnsSrvRecord), Attributes.EMPTY)
    }

    internal val name = name
        @TestOnly get

    override fun getServers(): List<ResolvedServerInfo> {
        try {
            val ctx = InitialDirContext(ENV)
            val resolved = ctx.getAttributes(name, ATTRIBUTE_IDS).get("srv").all
            val servers: MutableList<ResolvedServerInfo> = LinkedList()
            while (resolved.hasMore()) {
                servers.add(srvRecordToServerInfo(resolved.next() as String))
            }
            return servers
        } catch (e: Exception) {
            LOGGER.warn("", e)
        }
        return emptyList()
    }
}