package xyz.dowenliu.ketcd.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.AlarmResponse
import xyz.dowenliu.ketcd.api.DefragmentResponse
import xyz.dowenliu.ketcd.api.StatusResponse
import xyz.dowenliu.ketcd.endpoints
import xyz.dowenliu.ketcd.version.EtcdVersion
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
class EtcdMaintenanceServiceImplTest {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val assertion = Assertion()
    private val etcdClient = EtcdClient.newBuilder()
            .withEndpoint(*endpoints.map { Endpoint.of(it) }.toTypedArray())
            .build()
    private val service = etcdClient.newMaintenanceService()

    @BeforeClass
    fun assertIsImpl() {
        assertion.assertTrue(service is EtcdMaintenanceServiceImpl)
    }

    @AfterClass
    fun closeImpl() {
        service as EtcdMaintenanceServiceImpl
        service.close()
    }

    @Test
    fun testListAlarms() {
        val response = service.listAlarms()
        assertion.assertNotNull(response.header)
    }

    @Test
    fun testListAlarmsFuture() {
        val response = service.listAlarmsFuture().get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(response.header)
    }

    @Test
    fun testListAlarmsAsync() {
        val responseRef = AtomicReference<AlarmResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        service.listAlarmsAsync(object : ResponseCallback<AlarmResponse> {
            override fun onResponse(response: AlarmResponse) {
                responseRef.set(response)
                logger.debug("Alarm response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
            }

            override fun completeCallback() {
                logger.debug("Asynchronous listing alarms complete.")
                finishLatch.countDown()
            }
        })
        finishLatch.await(10, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertNotNull(response.header)
        } else {
            throw throwable!!
        }
    }

    @Test(enabled = false)
    fun testDeactiveAlarm() {
        // TEST_THIS how to test deactiveAlarm()?
    }

    @Test(enabled = false)
    fun testDeactiveAlarmFuture() {
        // TEST_THIS how to test deactiveAlarmFuture()?
    }

    @Test(enabled = false)
    fun testDeactiveAlarmAsync() {
        // TEST_THIS how to test deactiveAlarmAsync()?
    }

    @Test
    fun testDefragmentMember() {
        val response = service.defragmentMember()
        assertion.assertNotNull(response.header)
    }

    @Test
    fun testDefragmentMemberFuture() {
        val response = service.defragmentMemberFuture().get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(response.header)
    }

    @Test
    fun testDefragmentMemberAsync() {
        val responseRef = AtomicReference<DefragmentResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        service.defragmentMemberAsync(object : ResponseCallback<DefragmentResponse> {
            override fun onResponse(response: DefragmentResponse) {
                responseRef.set(response)
                logger.debug("Defragment response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
            }

            override fun completeCallback() {
                logger.debug("Asynchronous member defragmentation complete.")
                finishLatch.countDown()
            }
        })
        finishLatch.await(10, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertNotNull(response.header)
        } else {
            throw throwable!!
        }
    }

    @Test(groups = arrayOf("Maintenance.status"))
    fun testStatusMember() {
        val response = service.statusMember()
        assertion.assertNotNull(response.header)
        val version = response.version
        logger.info(version)
        assertion.assertTrue(version.startsWith("3."))
        assertion.assertNotNull(EtcdVersion.ofValue(version))
    }

    @Test
    fun testStatusMemberAsync() {
        val responseRef = AtomicReference<StatusResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        service.statusMemberAsync(object : ResponseCallback<StatusResponse> {
            override fun onResponse(response: StatusResponse) {
                responseRef.set(response)
                logger.debug("Status response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
            }

            override fun completeCallback() {
                logger.debug("Asynchronous member status complete.")
                finishLatch.countDown()
            }
        })
        finishLatch.await(10, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertNotNull(response.header)
            val version = response.version
            logger.info(version)
            assertion.assertTrue(version.startsWith("3."))
            assertion.assertNotNull(EtcdVersion.ofValue(version))
        } else {
            throw throwable!!
        }
    }
}