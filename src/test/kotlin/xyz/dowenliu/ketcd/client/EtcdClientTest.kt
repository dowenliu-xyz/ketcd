package xyz.dowenliu.ketcd.client

import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.endpoints

/**
 * create at 2017/4/14
 * @author liufl
 * @since 0.1.0
 */
class EtcdClientTest {
    private val assertion = Assertion()

    @Test
    fun test() {
        val etcdClient = EtcdClient.newBuilder().withEndpoint(*endpoints.map { Endpoint.of(it) }.toTypedArray()).build()

        val build1 = etcdClient.channelBuilder.build()
        val build2 = etcdClient.channelBuilder.build()

        assertion.assertNotSame(build1, build2)
    }
}