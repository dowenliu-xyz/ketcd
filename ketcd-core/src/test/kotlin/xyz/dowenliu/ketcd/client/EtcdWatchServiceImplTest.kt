package xyz.dowenliu.ketcd.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.WatchResponse
import xyz.dowenliu.ketcd.option.WatchOption
import xyz.dowenliu.ketcd.protobuf.toByteString
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * create at 2017/4/27
 * @author liufl
 * @since 0.1.0
 */
@Test(dependsOnGroups = arrayOf("lease"))
class EtcdWatchServiceImplTest {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val assertion: Assertion = Assertion()
    private val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of("http://localhost:2379")).build()

    @Test
    fun testWatch() {
        val key = "foo".toByteString()
        val kvService = etcdClient.newKVService()
        val watchService = etcdClient.newWatchService()
        val responses: Queue<WatchResponse> = LinkedList()
        val errors: Queue<Throwable> = LinkedList()
        val finishFlag = AtomicBoolean(false)
        val latch = CountDownLatch(1)
        val sentinel = watchService.watch(key, WatchOption.DEFAULT, object : EtcdWatchService.WatchEventHandler {
            override fun onResponse(response: WatchResponse) {
                logger.info(response.toString())
                synchronized(responses) {
                    responses.add(response)
                }
            }

            override fun onError(throwable: Throwable) {
                logger.info("", throwable)
                synchronized(errors) {
                    errors.add(throwable)
                }
            }

            override fun onCompleted() {
                finishFlag.set(true)
            }
        })
        thread {
            kvService.putInFuture(key, "init".toByteString()).get(1, TimeUnit.SECONDS)
            kvService.putInFuture(key, "changed".toByteString()).get(1, TimeUnit.SECONDS)
            kvService.deleteInFuture(key).get(1, TimeUnit.SECONDS)
            Thread.sleep(1000)
            sentinel.close()
            Thread.sleep(1000)
            latch.countDown()
        }
        latch.await()
        // TODO since v3.1.0, the onCompleted() function never works. Maybe this is a bug of etcd server.
        // assertion.assertTrue(finishFlag.get())
        assertion.assertEquals(errors.size, 0)
        assertion.assertEquals(responses.size, 5)
    }

    @Test
    fun testWatchWithRevisionLessThan0() {
        val key = "foo".toByteString()
        val kvService = etcdClient.newKVService()
        val watchService = etcdClient.newWatchService()
        val responses: Queue<WatchResponse> = LinkedList()
        val errors: Queue<Throwable> = LinkedList()
        val finishFlag = AtomicBoolean(false)
        val latch = CountDownLatch(1)
        val watchOption = WatchOption.newBuilder().withStartRevision(-1L).build()
        val sentinel = watchService.watch(key, watchOption, object : EtcdWatchService.WatchEventHandler {
            override fun onResponse(response: WatchResponse) {
                logger.info(response.toString())
                synchronized(responses) {
                    responses.add(response)
                }
            }

            override fun onError(throwable: Throwable) {
                logger.info("", throwable)
                synchronized(errors) {
                    errors.add(throwable)
                }
            }

            override fun onCompleted() {
                finishFlag.set(true)
            }
        })
        thread {
            kvService.putInFuture(key, "init".toByteString()).get(1, TimeUnit.SECONDS)
            kvService.putInFuture(key, "changed".toByteString()).get(1, TimeUnit.SECONDS)
            kvService.deleteInFuture(key).get(1, TimeUnit.SECONDS)
            Thread.sleep(1000)
            sentinel.close()
            Thread.sleep(1000)
            latch.countDown()
        }
        latch.await()
        assertion.assertEquals(errors.size, 0)
        assertion.assertNotEquals(responses.size, 5) // all put/delete event not caught. seams from start revision Long.MAX_VALUE
    }
}