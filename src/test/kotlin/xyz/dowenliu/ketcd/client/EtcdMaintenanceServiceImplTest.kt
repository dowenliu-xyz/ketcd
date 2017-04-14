package xyz.dowenliu.ketcd.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.AlarmResponse
import xyz.dowenliu.ketcd.endpoints
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
    fun testListAlarmsAsync() {
        val responseRef  = AtomicReference<AlarmResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        service.listAlarmsAsync(object : EtcdMaintenanceService.AlarmListCallback {
            override fun onResponse(response: AlarmResponse) {
                responseRef.set(response)
                logger.debug("Alarm response received.")
                finishLatch.countDown()
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                finishLatch.countDown()
            }

            override fun completeCallback() {
                logger.debug("Asynchronous listing alarms complete.")
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
}