package xyz.dowenliu.ketcd.client

import io.grpc.StatusRuntimeException
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.UsernamePassword
import xyz.dowenliu.ketcd.api.Permission
import xyz.dowenliu.ketcd.protobuf.toByteString
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * create at 2017/4/17
 * @author liufl
 * @since 0.1.0
 */
@Test(dependsOnGroups = arrayOf("kv"))
class EtcdAuthServiceImplTest {
    private val assertion = Assertion()
    private val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of("localhost:2379")).build()
    private lateinit var authEtcdClient: EtcdClient

    companion object {
        private val roleName = "root".toByteString()

        private val keyRangeBegin = "foo".toByteString()
        private val keyRangeEnd = "zoo".toByteString()

        private val testKey = "foo1".toByteString()
        private val testName = "bar".toByteString()

        private val userName = "root".toByteString()
        private val password = "123".toByteString()
    }

    @Test(groups = arrayOf("role"))
    fun testRoleAdd() {
        val authService = etcdClient.newAuthService()
        val response = authService.roleAddInFuture(roleName.toStringUtf8()).get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(response.header)
    }

    @Test(dependsOnMethods = arrayOf("testRoleAdd"), groups = arrayOf("role"))
    fun testRoleGrantPermission() {
        val authService = etcdClient.newAuthService()
        val response = authService.roleGrantPermissionInFuture(roleName.toStringUtf8(), Permission.Type.READWRITE,
                keyRangeBegin, keyRangeEnd).get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(response.header)
    }

    @Test(groups = arrayOf("user"))
    fun testUserAdd() {
        val authService = etcdClient.newAuthService()
        val response = authService.userAddInFuture(UsernamePassword
                .of(userName.toStringUtf8(), password.toStringUtf8())).get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(response.header)
    }

    @Test(dependsOnMethods = arrayOf("testUserAdd", "testRoleGrantPermission"), groups = arrayOf("user"))
    fun testUserGrantRole() {
        val authService = etcdClient.newAuthService()
        val response = authService.userGrantRoleInFuture(userName.toStringUtf8(), roleName.toStringUtf8())
                .get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(response.header)
    }

    @Test(dependsOnGroups = arrayOf("user"), groups = arrayOf("authEnable"))
    fun testEnableAuth() {
        val authService = etcdClient.newAuthService()
        val response = authService.authEnableInFuture().get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(response.header)
    }

    @Test(dependsOnMethods = arrayOf("testEnableAuth"), groups = arrayOf("authEnable"))
    fun setupAuthClient() {
        authEtcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of("localhost:2379"))
                .withUsernamePassword(UsernamePassword.of(userName.toStringUtf8(), password.toStringUtf8()))
                .build()
    }

    @Test(groups = arrayOf("testAuth"), dependsOnGroups = arrayOf("authEnable"))
    fun testKV() {
        var err: Throwable? = null
        try {
            authEtcdClient.newKVService().putInFuture(testKey, testName).get(5, TimeUnit.SECONDS)
            val rangeResponse = this.authEtcdClient.newKVService().getInFuture(testKey).get(5, TimeUnit.SECONDS)
            assertion.assertTrue(rangeResponse.count != 0L && rangeResponse.getKvs(0).value == testName)
        } catch (e: StatusRuntimeException) {
            err = e
        }
        assertion.assertNull(err)
        try {
            etcdClient.newKVService().putInFuture(testKey, testName).get(5, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            err = e
        }
        assertion.assertNotNull(err)
        try {
            etcdClient.newKVService().getInFuture(testKey).get(5, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            err = e
        }
        assertion.assertNotNull(err)
    }

    @Test(groups = arrayOf("testAuth"), dependsOnGroups = arrayOf("authEnable"))
    fun testRoleGet() {
        val roleGetResponse = authEtcdClient.newAuthService().roleGetInFuture(roleName.toStringUtf8())
                .get(5, TimeUnit.SECONDS)
        assertion.assertTrue(roleGetResponse.permCount != 0)
    }

    @Test(dependsOnGroups = arrayOf("testAuth"), groups = arrayOf("disableAuth"))
    fun testDisableAuth() {
        var err: Throwable? = null
        try {
            authEtcdClient.newAuthService().authDisableInFuture().get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            err = e
        }
        assertion.assertNull(err)
    }

    @Test(dependsOnGroups = arrayOf("disableAuth"), groups = arrayOf("clearEnv"))
    fun delUser() {
        var err: Throwable? = null
        try {
            etcdClient.newAuthService().userDeleteInFuture(userName.toStringUtf8()).get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            err = e
        }
        assertion.assertNull(err)
    }

    @Test(dependsOnGroups = arrayOf("disableAuth"), groups = arrayOf("clearEnv"))
    fun delRole() {
        var err: Throwable? = null
        try {
            etcdClient.newAuthService().roleDeleteInFuture(roleName.toStringUtf8()).get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            err = e
        }
        assertion.assertNull(err)
    }
}