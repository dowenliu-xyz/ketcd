package xyz.dowenliu.ketcd.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.LeaseGrantResponse
import xyz.dowenliu.ketcd.api.LeaseRevokeResponse
import xyz.dowenliu.ketcd.api.LeaseTimeToLiveResponse
import xyz.dowenliu.ketcd.exception.LeaseNotFoundException
import xyz.dowenliu.ketcd.option.PutOption
import xyz.dowenliu.ketcd.protobuf.toByteString
import xyz.dowenliu.ketcd.version.EtcdVersion
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * create at 2017/4/26
 * @author liufl
 * @since 0.1.0
 */
@Test(dependsOnGroups = arrayOf("auth"), groups = arrayOf("lease"))
class EtcdLeaseServiceImplTest {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val assertion = Assertion()
    private val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of("http://localhost:2379")).build()

    companion object {
        private val testKey1 = "foo1".toByteString()
        private val testKey2 = "foo2".toByteString()
        private val testKey3 = "foo3".toByteString()
        private val testValue = "bar".toByteString()
    }

    @Test(groups = arrayOf("leaseGrant"))
    fun testGrant() {
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val grantResp = leaseService.grant(3)
        assertion.assertNotNull(grantResp.header)
        val leaseId = grantResp.id
        logger.debug(leaseId.toString(16))
        logger.debug(grantResp.ttl.toString())
        assertion.assertNotNull(leaseId)
        assertion.assertNotEquals(leaseId, 0L)
        assertion.assertTrue(grantResp.error.isNullOrBlank())
        kvService.putInFuture(testKey1, testValue,
                PutOption.newBuilder().withLeaseId(leaseId).build())
                .get(5, TimeUnit.SECONDS)
        assertion.assertEquals(kvService.getInFuture(testKey1).get(5, TimeUnit.SECONDS).count, 1L)
        Thread.sleep(4000)
        assertion.assertEquals(kvService.getInFuture(testKey1).get(5, TimeUnit.SECONDS).count, 0L)
    }

    @Test(groups = arrayOf("leaseGrant"))
    fun testGrantInFuture() {
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val grantResp = leaseService.grantInFuture(3).get(3, TimeUnit.SECONDS)
        assertion.assertNotNull(grantResp.header)
        val leaseId = grantResp.id
        logger.debug(leaseId.toString(16))
        logger.debug(grantResp.ttl.toString())
        assertion.assertNotNull(leaseId)
        assertion.assertNotEquals(leaseId, 0L)
        assertion.assertTrue(grantResp.error.isNullOrBlank())
        kvService.putInFuture(testKey2, testValue,
                PutOption.newBuilder().withLeaseId(leaseId).build())
                .get(5, TimeUnit.SECONDS)
        assertion.assertEquals(kvService.getInFuture(testKey2).get(5, TimeUnit.SECONDS).count, 1L)
        Thread.sleep(4000)
        assertion.assertEquals(kvService.getInFuture(testKey2).get(5, TimeUnit.SECONDS).count, 0L)
    }

    @Test(groups = arrayOf("leaseGrant"))
    fun testGrantAsync() {
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val responseRef = AtomicReference<LeaseGrantResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        leaseService.grantAsync(3, callback = object : ResponseCallback<LeaseGrantResponse> {
            override fun onResponse(response: LeaseGrantResponse) {
                responseRef.set(response)
                logger.debug("A lease grant response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                logger.error("", throwable)
            }

            override fun completeCallback() {
                finishLatch.countDown()
                logger.debug("Asynchronously lease grant operation finished.")
            }
        })
        finishLatch.await(3, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (throwable != null) throw throwable
        val grantResp = response!!
        assertion.assertNotNull(grantResp.header)
        val leaseId = grantResp.id
        logger.debug(leaseId.toString(16))
        logger.debug(grantResp.ttl.toString())
        assertion.assertNotNull(leaseId)
        assertion.assertNotEquals(leaseId, 0L)
        assertion.assertTrue(grantResp.error.isNullOrBlank())
        kvService.putInFuture(testKey3, testValue,
                PutOption.newBuilder().withLeaseId(leaseId).build())
                .get(5, TimeUnit.SECONDS)
        assertion.assertEquals(kvService.getInFuture(testKey3).get(5, TimeUnit.SECONDS).count, 1L)
        Thread.sleep(4000)
        assertion.assertEquals(kvService.getInFuture(testKey3).get(5, TimeUnit.SECONDS).count, 0L)
    }

    @Test(dependsOnGroups = arrayOf("leaseGrant"), groups = arrayOf("leaseTimeToLive"))
    fun testTimeToLive() {
        val etcdVersion = etcdClient.knowVersion.get() ?: throw IllegalStateException("Unknown etcd version.")
        if (etcdVersion.releaseNumber < EtcdVersion.V3_1_0_alpha0.releaseNumber) return
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val leaseId = leaseService.grantInFuture(5).get(5, TimeUnit.SECONDS).id
        try {
            kvService.putInFuture(testKey1, testValue, PutOption.newBuilder().withLeaseId(leaseId).build())
                    .get(5, TimeUnit.SECONDS)
            var ttlResp = leaseService.timeToLive(leaseId)
            assertion.assertEquals(ttlResp.id, leaseId)
            assertion.assertEquals(ttlResp.grantedTTL, 5L)
            assertion.assertTrue(ttlResp.ttl <= 5L)
            assertion.assertEquals(ttlResp.keysCount, 0)
            ttlResp = leaseService.timeToLive(leaseId, true)
            assertion.assertNotEquals(ttlResp.keysCount, 0)
        } finally {
            kvService.deleteInFuture(testKey1).get()
        }
    }

    @Test(dependsOnGroups = arrayOf("leaseGrant"), groups = arrayOf("leaseTimeToLive"))
    fun testTimeToLiveInFuture() {
        val etcdVersion = etcdClient.knowVersion.get() ?: throw IllegalStateException("Unknown etcd version.")
        if (etcdVersion.releaseNumber < EtcdVersion.V3_1_0_alpha0.releaseNumber) return
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val leaseId = leaseService.grantInFuture(5).get(5, TimeUnit.SECONDS).id
        try {
            kvService.putInFuture(testKey2, testValue, PutOption.newBuilder().withLeaseId(leaseId).build())
                    .get(5, TimeUnit.SECONDS)
            var ttlResp = leaseService.timeToLiveInFuture(leaseId).get(2, TimeUnit.SECONDS)
            assertion.assertEquals(ttlResp.id, leaseId)
            assertion.assertEquals(ttlResp.grantedTTL, 5L)
            assertion.assertTrue(ttlResp.ttl <= 5L)
            assertion.assertEquals(ttlResp.keysCount, 0)
            ttlResp = leaseService.timeToLiveInFuture(leaseId, true).get(2, TimeUnit.SECONDS)
            assertion.assertNotEquals(ttlResp.keysCount, 0)
        } finally {
            kvService.deleteInFuture(testKey2).get()
        }
    }

    @Test(dependsOnGroups = arrayOf("leaseGrant"), groups = arrayOf("leaseTimeToLive"))
    fun testTimeToLiveAsync() {
        val etcdVersion = etcdClient.knowVersion.get() ?: throw IllegalStateException("Unknown etcd version.")
        if (etcdVersion.releaseNumber < EtcdVersion.V3_1_0_alpha0.releaseNumber) return
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val leaseId = leaseService.grantInFuture(5).get(5, TimeUnit.SECONDS).id
        try {
            kvService.putInFuture(testKey3, testValue, PutOption.newBuilder().withLeaseId(leaseId).build())
                    .get(5, TimeUnit.SECONDS)
            val respRef = AtomicReference<LeaseTimeToLiveResponse?>()
            val errorRef = AtomicReference<Throwable?>()
            val finishLatch = CountDownLatch(1)
            leaseService.timeToLiveAsync(leaseId, callback = object : ResponseCallback<LeaseTimeToLiveResponse> {
                override fun onResponse(response: LeaseTimeToLiveResponse) {
                    respRef.set(response)
                    logger.debug("A lease timeToLive response received.")
                }

                override fun onError(throwable: Throwable) {
                    errorRef.set(throwable)
                    logger.error("", throwable)
                }

                override fun completeCallback() {
                    finishLatch.countDown()
                    logger.debug("Asynchronously lease timeToLive request finished.")
                }
            })
            finishLatch.await(2, TimeUnit.SECONDS)
            var response = respRef.get()
            var throwable = errorRef.get()
            assertion.assertTrue(response != null || throwable != null)
            if (throwable != null) throw throwable
            var ttlResp = response!!
            assertion.assertEquals(ttlResp.id, leaseId)
            assertion.assertEquals(ttlResp.grantedTTL, 5L)
            assertion.assertTrue(ttlResp.ttl <= 5L)
            assertion.assertEquals(ttlResp.keysCount, 0)
            respRef.set(null)
            errorRef.set(null)
            val finishLatch2 = CountDownLatch(1)
            leaseService.timeToLiveAsync(leaseId, true, object : ResponseCallback<LeaseTimeToLiveResponse> {
                override fun onResponse(response: LeaseTimeToLiveResponse) {
                    respRef.set(response)
                    logger.debug("A lease timeToLive response received.")
                }

                override fun onError(throwable: Throwable) {
                    errorRef.set(throwable)
                    logger.error("", throwable)
                }

                override fun completeCallback() {
                    finishLatch2.countDown()
                    logger.debug("Asynchronously lease timeToLive request finished.")
                }
            })
            finishLatch2.await(2, TimeUnit.SECONDS)
            response = respRef.get()
            throwable = errorRef.get()
            assertion.assertTrue(response != null || throwable != null)
            if (throwable != null) throw throwable
            ttlResp = leaseService.timeToLive(leaseId, true)
            assertion.assertNotEquals(ttlResp.keysCount, 0)
        } finally {
            kvService.deleteInFuture(testKey3).get()
        }
    }

    @Test(dependsOnGroups = arrayOf("leaseTimeToLive"), groups = arrayOf("leaseRevoke"))
    fun testRevoke() {
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val leasId = leaseService.grantInFuture(5).get(5, TimeUnit.SECONDS).id
        kvService.putInFuture(testKey1, testValue, PutOption.newBuilder().withLeaseId(leasId).build())
                .get(2, TimeUnit.SECONDS)
        try {
            assertion.assertEquals(kvService.getInFuture(testKey1).get(2, TimeUnit.SECONDS).count, 1L)
            leaseService.revoke(leasId)
            assertion.assertEquals(kvService.getInFuture(testKey1).get(2, TimeUnit.SECONDS).count, 0L)
        } finally {
            kvService.deleteInFuture(testKey1).get()
        }
    }

    @Test(dependsOnGroups = arrayOf("leaseTimeToLive"), groups = arrayOf("leaseRevoke"))
    fun testRevokeInFuture() {
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val leasId = leaseService.grantInFuture(5).get(5, TimeUnit.SECONDS).id
        kvService.putInFuture(testKey2, testValue, PutOption.newBuilder().withLeaseId(leasId).build())
                .get(2, TimeUnit.SECONDS)
        try {
            assertion.assertEquals(kvService.getInFuture(testKey2).get(2, TimeUnit.SECONDS).count, 1L)
            leaseService.revokeInFuture(leasId).get(2, TimeUnit.SECONDS)
            assertion.assertEquals(kvService.getInFuture(testKey2).get(2, TimeUnit.SECONDS).count, 0L)
        } finally {
            kvService.deleteInFuture(testKey2).get()
        }
    }

    @Test(dependsOnGroups = arrayOf("leaseTimeToLive"), groups = arrayOf("leaseRevoke"))
    fun testRevokeAsync() {
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val leasId = leaseService.grantInFuture(5).get(5, TimeUnit.SECONDS).id
        kvService.putInFuture(testKey3, testValue, PutOption.newBuilder().withLeaseId(leasId).build())
                .get(2, TimeUnit.SECONDS)
        try {
            assertion.assertEquals(kvService.getInFuture(testKey3).get(2, TimeUnit.SECONDS).count, 1L)
            val respRef = AtomicReference<LeaseRevokeResponse?>()
            val errorRef = AtomicReference<Throwable?>()
            val finishLatch = CountDownLatch(1)
            leaseService.revokeAsync(leasId, object : ResponseCallback<LeaseRevokeResponse> {
                override fun onResponse(response: LeaseRevokeResponse) {
                    respRef.set(response)
                    logger.debug("A lease revoke response received.")
                }

                override fun onError(throwable: Throwable) {
                    errorRef.set(throwable)
                    logger.error("", throwable)
                }

                override fun completeCallback() {
                    finishLatch.countDown()
                    logger.debug("Asynchronously lease revoke request finished.")
                }
            })
            finishLatch.await(2, TimeUnit.SECONDS)
            assertion.assertEquals(kvService.getInFuture(testKey3).get(2, TimeUnit.SECONDS).count, 0L)
        } finally {
            kvService.deleteInFuture(testKey3).get()
        }
    }

    @Test(dependsOnGroups = arrayOf("leaseRevoke"))
    fun testKeepAlive() {
        val kvService = etcdClient.newKVService()
        val leaseService = etcdClient.newLeaseService()
        val leaseId = leaseService.grantInFuture(5).get(2, TimeUnit.SECONDS).id
        kvService.putInFuture(testKey1, testValue, PutOption.newBuilder().withLeaseId(leaseId).build())
                .get(2, TimeUnit.SECONDS)
        try {
            assertion.assertEquals(kvService.getInFuture(testKey1).get(1, TimeUnit.SECONDS).count, 1L)
            val sentinel = leaseService.keepAlive(leaseId)
            Thread.sleep(6000)
            assertion.assertEquals(kvService.getInFuture(testKey1).get(1, TimeUnit.SECONDS).count, 1L)
            sentinel.close()
            Thread.sleep(6000)
            assertion.assertEquals(kvService.getInFuture(testKey1).get(1, TimeUnit.SECONDS).count, 0L)
        } finally {
            kvService.deleteInFuture(testKey1).get()
        }
    }

    @Test
    fun testKeepAliveNotFoundLease() {
        val leaseService = etcdClient.newLeaseService()
        try {
            leaseService.keepAlive(999999L)
            Thread.sleep(5000)
            assertion.fail()
        } catch (t: Throwable) {
            assertion.assertTrue(t is LeaseNotFoundException)
        }
    }
}