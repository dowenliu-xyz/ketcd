package xyz.dowenliu.ketcd.client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.UsernamePassword
import xyz.dowenliu.ketcd.api.AuthGrpc
import xyz.dowenliu.ketcd.api.AuthenticateRequest
import xyz.dowenliu.ketcd.exception.AuthFailedException
import xyz.dowenliu.ketcd.exception.ConnectException
import xyz.dowenliu.ketcd.resolver.AbstractEtcdNameResolverFactory
import java.util.concurrent.ExecutionException

/**
 * Etcd client.
 *
 * create at 2017/4/13
 * @author liufl
 * @since 0.1.0
 */
class EtcdClient(private val channelBuilder: ManagedChannelBuilder<*>,
                 private val token: String?) {
    companion object {
        /**
         * Creates new Builder instance.
         *
         * @return The instance create.
         */
        @JvmStatic fun newBuilder() = Builder()

        @Throws(ConnectException::class, AuthFailedException::class)
        private fun getToken(channel: ManagedChannel, builder: Builder): String? {
            val usernamePassword = builder.usernamePassword ?: return null
            try {
                return authenticate(channel, usernamePassword).token
            } catch (e: InterruptedException) {
                throw ConnectException("connect to etcd failed.", e)
            } catch (e: ExecutionException) {
                throw AuthFailedException("auth failed as wrong username or password", e)
            }
        }

        private fun authenticate(channel: ManagedChannel, usernamePassword: UsernamePassword) =
                AuthGrpc.newBlockingStub(channel).authenticate(
                        AuthenticateRequest.newBuilder()
                                .setNameBytes(usernamePassword.username)
                                .setPasswordBytes(usernamePassword.password)
                                .build()
                )
    }

    class Builder internal constructor() {
        private val _endpoints: MutableList<Endpoint> = mutableListOf()
        /**
         * Etcd auth username password info.
         */
        var usernamePassword: UsernamePassword? = null
            private set
        /**
         * NameResolver factory for etcd client.
         */
        var nameResolverFactory: AbstractEtcdNameResolverFactory? = null
            private set
        /**
         * Channel builder.
         */
        var channelBuilder: ManagedChannelBuilder<*>? = null
            private set
        /**
         * The distinct list of endpoints configured for the builder.
         */
        val endpoints = _endpoints.distinct().toList()

        /**
         * Add etcd server endpoints.
         *
         * @param endpoints Etcd endpoints.
         * @return this builder to train
         */
        fun withEndpoint(vararg endpoints: Endpoint): Builder {
            _endpoints.addAll(endpoints)
            return this
        }

        /**
         * Config etcd auth username password info.
         *
         * @param usernamePassword etcd auth username password info.
         * @return this builder to train.
         */
        fun withUsernamePassword(usernamePassword: UsernamePassword?): Builder {
            this.usernamePassword = usernamePassword
            return this
        }

        /**
         * Config etcd name resolver factory for etcd client.
         *
         * @param nameResolverFactory AbstractEtcdNameResolverFactory instance to use.
         * @return this builder to train.
         */
        fun withNameResolverFactory(nameResolverFactory: AbstractEtcdNameResolverFactory?): Builder {
            this.nameResolverFactory = nameResolverFactory
            return this
        }

        /**
         * Config channel builder.
         *
         * @param channelBuilder ManagedChannelBuilder instance to use.
         * @return this builder to train.
         */
        fun withCannelBuilder(channelBuilder: ManagedChannelBuilder<*>?): Builder {
            this.channelBuilder = channelBuilder
            return this
        }

        /**
         * Creates new EtcdClientProvider instance.
         *
         * @return The instance create.
         */
        fun build(): EtcdClient {
            val channelBuilder = channelBuilder ?:
                    defaultChannelBuilder(nameResolverFactory ?: simpleNameResolverFactory(endpoints))
            val token = getToken(channelBuilder.build(), this)
            return EtcdClient(channelBuilder, token)
        }
    }
}