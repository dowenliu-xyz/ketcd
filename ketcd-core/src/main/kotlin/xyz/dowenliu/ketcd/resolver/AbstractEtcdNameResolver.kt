package xyz.dowenliu.ketcd.resolver

import io.grpc.Attributes
import io.grpc.NameResolver
import io.grpc.ResolvedServerInfo
import io.grpc.ResolvedServerInfoGroup
import io.grpc.internal.SharedResourceHolder
import java.net.URI
import java.util.concurrent.ExecutorService
import javax.annotation.concurrent.GuardedBy

/**
 * The abstract ectd name resolver, all other name resolvers should extend
 * this one instead of [NameResolver]
 *
 * create at 2017/4/9
 * @author liufl
 * @since 3.1.0
 */
abstract class AbstractEtcdNameResolver(name: String,
                                        private val executorResource: SharedResourceHolder.Resource<ExecutorService>) :
        NameResolver() {
    private val authority: String
    private val resolutionRunnable: Runnable = ResolverTask()

    @GuardedBy("this") private var shutdown: Boolean = false
    @GuardedBy("this") private var resolving: Boolean = false
    @GuardedBy("this") private var listener: Listener? = null
    @GuardedBy("this") private var executor: ExecutorService? = null

    init {
        val nameUri = URI.create("//$name")
        authority = checkNotNull(nameUri.authority, { "nameUri ($nameUri) doesn't have an authority" })
    }

    override fun getServiceAuthority(): String = authority

    @Synchronized override fun start(listener: Listener?) {
        check(this.listener == null, { "already started" })
        this.executor = SharedResourceHolder.get(executorResource)
        this.listener = requireNotNull(listener, { "listener should not be null" })
        resolve()
    }

    @Synchronized override fun refresh() {
        check(listener != null, { "not started" })
        resolve()
    }

    @Synchronized override fun shutdown() {
        if (shutdown) return
        shutdown = true
        if (executor != null) executor = SharedResourceHolder.release(executorResource, executor)
    }

    @GuardedBy("this") private fun resolve() {
        if (resolving || shutdown) return
        executor?.execute(resolutionRunnable) // executor should be not null now.
    }

    internal abstract fun getServers(): List<ResolvedServerInfo>

    /**
     * Helper task to resolve servers.
     */
    private inner class ResolverTask : Runnable {
        override fun run() {
            var _savedListener: Listener? = null
            synchronized(this@AbstractEtcdNameResolver) {
                if (shutdown) return
                resolving = true
                _savedListener = listener
            }
            val savedListener = _savedListener // savedListener should be not null now.
            try {
                val servers = getServers()
                savedListener
                        ?.onUpdate(listOf(ResolvedServerInfoGroup.builder().addAll(servers).build()), Attributes.EMPTY)
            } finally {
                synchronized(this@AbstractEtcdNameResolver) { resolving = false }
            }
        }
    }
}