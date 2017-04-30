package xyz.dowenliu.ketcd.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.endpoints
import xyz.dowenliu.ketcd.peerUrls
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Test etcd cluster service.
 *
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
class EtcdClusterServiceImplTest {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val assertion = Assertion()

    private var addedMember: Member? = null

    @Test
    fun testListClusterInFuture() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(*endpoints.map { Endpoint.of(it) }.toTypedArray()).build()
        val clusterService = etcdClient.newClusterService()
        val response = clusterService.listMemberInFuture().get(5, TimeUnit.SECONDS)
        assertion.assertEquals(response.membersCount, 3, "Members: ${response.membersCount}")
    }

    @Test(dependsOnMethods = arrayOf("testListClusterInFuture"))
    fun testAddMemberInFuture() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[0]), Endpoint.of(endpoints[1])).build()
        val clusterService = etcdClient.newClusterService()
        val response = clusterService.listMemberInFuture().get(5, TimeUnit.SECONDS)
        assertion.assertEquals(response.membersCount, 3)
        val addResponse = clusterService.addMemberInFuture(peerUrls.copyOfRange(2, 3).map { Endpoint.of(it) }.toTypedArray())
                .get(5, TimeUnit.SECONDS)
        val member = addResponse.member
        assertion.assertNotNull(member, "added member: ${member.id}")
        addedMember = member
    }

    @Test(dependsOnMethods = arrayOf("testAddMemberInFuture"))
    fun testUpdateMemberInFuture() {
        var throwable: Throwable? = null
        try {
            val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[1]), Endpoint.of(endpoints[2])).build()
            val clusterService = etcdClient.newClusterService()
            val response = clusterService.listMemberInFuture().get(5, TimeUnit.SECONDS)
            clusterService.updateMemberInFuture(response.getMembers(0).id, arrayOf(Endpoint.of("http://localhost:12380"))).get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            LoggerFactory.getLogger(javaClass).error("", e)
            throwable = e
        }
        assertion.assertNull(throwable, "update for member")
    }

    @Test(dependsOnMethods = arrayOf("testUpdateMemberInFuture"))
    fun testDeleteMemberInFuture() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[0]), Endpoint.of(endpoints[1])).build()
        val clusterService = etcdClient.newClusterService()
        val member = addedMember ?: return
        clusterService.removeMemberInFuture(member.id).get(5, TimeUnit.SECONDS)
        val newCount = clusterService.listMemberInFuture().get(5, TimeUnit.SECONDS).membersCount
        assertion.assertEquals(newCount, 3, "delete added member (${member.id}), and left $newCount members.")
    }

    @Test(enabled = false)
    fun testListCluster() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(*endpoints.map { Endpoint.of(it) }.toTypedArray()).build()
        val clusterService = etcdClient.newClusterService()
        val response = clusterService.listMember()
        assertion.assertEquals(response.membersCount, 3, "Members: ${response.membersCount}")
    }

    @Test(dependsOnMethods = arrayOf("testListCluster"), enabled = false)
    fun testAddMember() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[0]), Endpoint.of(endpoints[1])).build()
        val clusterService = etcdClient.newClusterService()
        val response = clusterService.listMember()
        assertion.assertEquals(response.membersCount, 3)
        val addResponse = clusterService.addMember(peerUrls.copyOfRange(1, 2).map { Endpoint.of(it) }.toTypedArray())
        val member = addResponse.member
        assertion.assertNotNull(member, "added member: ${member.id}")
        addedMember = member
    }

    @Test(dependsOnMethods = arrayOf("testAddMember"), enabled = false  )
    fun testUpdateMember() {
        var throwable: Throwable? = null
        try {
            val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[1]), Endpoint.of(endpoints[2])).build()
            val clusterService = etcdClient.newClusterService()
            val response = clusterService.listMember()
            clusterService.updateMember(response.getMembers(0).id, arrayOf(Endpoint.of("http://localhost:12380")))
        } catch (e: Exception) {
            LoggerFactory.getLogger(javaClass).error("", e)
            throwable = e
        }
        assertion.assertNull(throwable, "update for member")
    }

    @Test(dependsOnMethods = arrayOf("testUpdateMember"), enabled = false   )
    fun testDeleteMember() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[0]), Endpoint.of(endpoints[1])).build()
        val clusterService = etcdClient.newClusterService()
        val member = addedMember ?: return
        clusterService.removeMember(member.id)
        val newCount = clusterService.listMember().membersCount
        assertion.assertEquals(newCount, 3, "delete added member (${member.id}), and left $newCount members.")
    }

    @Test(enabled = false)
    fun testListClusterAsync() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(*endpoints.map { Endpoint.of(it) }.toTypedArray()).build()
        val clusterService = etcdClient.newClusterService()
        val responseRef: AtomicReference<MemberListResponse?> = AtomicReference()
        val errorRef: AtomicReference<Throwable?> = AtomicReference()
        val finishLatch = CountDownLatch(1)
        clusterService.listMemberAsync(object : ResponseCallback<MemberListResponse> {
            override fun onResponse(response: MemberListResponse) {
                responseRef.set(response)
                logger.debug("Member list response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
            }

            override fun completeCallback() {
                logger.debug("Asynchronously member listing completed.")
                finishLatch.countDown()
            }
        })
        finishLatch.await(10, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertEquals(response.membersCount, 3, "Members: ${response.membersCount}")
        } else
            throw throwable!!
    }

    @Test(dependsOnMethods = arrayOf("testListClusterAsync"), enabled = false)
    fun testAddMemberAsync() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[0]), Endpoint.of(endpoints[1])).build()
        val clusterService = etcdClient.newClusterService()
        assertion.assertEquals(clusterService.listMember().membersCount, 3)
        val responseRef: AtomicReference<MemberAddResponse?> = AtomicReference()
        val errorRef: AtomicReference<Throwable?> = AtomicReference()
        val finishLatch = CountDownLatch(1)
        clusterService.addMemberAsync(arrayOf(Endpoint.of(peerUrls[2])), object : ResponseCallback<MemberAddResponse> {
            override fun onResponse(response: MemberAddResponse) {
                responseRef.set(response)
                logger.debug("Member add response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
            }

            override fun completeCallback() {
                logger.debug("Asynchronously member adding completed.")
                finishLatch.countDown()
            }
        })

        finishLatch.await(10, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            val member = response.member
            assertion.assertNotNull(member, "added member: ${member.id}")
            addedMember = member
        } else
            throw throwable!!
    }

    @Test(dependsOnMethods = arrayOf("testAddMemberAsync"), enabled = false)
    fun testUpdateMemberAsync() {
        var throwable: Throwable? = null
        try {
            val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[1]), Endpoint.of(endpoints[2])).build()
            val clusterService = etcdClient.newClusterService()
            val response = clusterService.listMember()
            val responseRef: AtomicReference<MemberUpdateResponse?> = AtomicReference()
            val errorRef: AtomicReference<Throwable?> = AtomicReference()
            val finishLatch = CountDownLatch(1)
            clusterService.updateMemberAsync(response.getMembers(0).id, arrayOf(Endpoint.of("http://localhost:12380")),
                    object : ResponseCallback<MemberUpdateResponse> {
                        override fun onResponse(response: MemberUpdateResponse) {
                            responseRef.set(response)
                            logger.debug("Member update response received.")
                        }

                        override fun onError(throwable: Throwable) {
                            errorRef.set(throwable)
                        }

                        override fun completeCallback() {
                            logger.debug("Asynchronously member updating completed.")
                            finishLatch.countDown()
                        }
                    })
            finishLatch.await(10, TimeUnit.SECONDS)
            val error = errorRef.get()
            if (error != null)
                throw error
        } catch (e: Exception) {
            LoggerFactory.getLogger(javaClass).error("", e)
            throwable = e
        }
        assertion.assertNull(throwable, "update for member")
    }

    @Test(dependsOnMethods = arrayOf("testUpdateMemberAsync"), enabled = false)
    fun testDeleteMemberAsync() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of(endpoints[0]), Endpoint.of(endpoints[1])).build()
        val clusterService = etcdClient.newClusterService()
        val member = addedMember ?: return
        val errorRef: AtomicReference<Throwable?> = AtomicReference()
        val finishLatch = CountDownLatch(1)
        clusterService.removeMemberAsync(member.id, object : ResponseCallback<MemberRemoveResponse> {
            override fun onResponse(response: MemberRemoveResponse) {
                logger.debug("Member remove response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
            }

            override fun completeCallback() {
                logger.debug("Asynchronously member removing completed.")
                finishLatch.countDown()
            }
        })
        finishLatch.await(10, TimeUnit.SECONDS)
        val newCount = clusterService.listMember().membersCount
        assertion.assertEquals(newCount, 3, "delete added member (${member.id}), and left $newCount members.")
    }
}