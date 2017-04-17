package xyz.dowenliu.ketcd.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
import org.testng.asserts.Assertion
import xyz.dowenliu.ketcd.Endpoint
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.kv.*
import xyz.dowenliu.ketcd.kv.option.CompactOption
import xyz.dowenliu.ketcd.kv.option.DeleteOption
import xyz.dowenliu.ketcd.kv.option.GetOption
import xyz.dowenliu.ketcd.kv.option.PutOption
import xyz.dowenliu.ketcd.protobuf.toByteString
import xyz.dowenliu.ketcd.version.EtcdVersion
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * create at 2017/4/17
 * @author liufl
 * @since 0.1.0
 */
class EtcdKVServiceImplTest {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val assertion = Assertion()
    private val etcdClient = EtcdClient.newBuilder().withEndpoint(Endpoint.of("http://localhost:2379")).build()

    companion object {
        val key = "key".toByteString()
        val key1 = "key1".toByteString()
        val baseKeyZ = "keyZ".toByteString()
        val value = "value".toByteString()
        val value1 = "value1".toByteString()
        val value2 = "value2".toByteString()
    }

    @Test
    fun testPut() {
        val kvService = etcdClient.newKVService()
        // put with default options
        var response = kvService.put(key, value)
        assertion.assertTrue(response.hasHeader())
        assertion.assertTrue(!response.hasPrevKv())
        // put overwrite
        response = kvService.put(key, value1)
        assertion.assertTrue(response.hasHeader())
        assertion.assertTrue(!response.hasPrevKv())
        // put with prevKV
        var options = PutOption.newBuilder().prevKV(true).build()
        response = kvService.put(key, value2, options)
        assertion.assertTrue(response.hasHeader())
        if (EtcdClient.knowVersion.get()?.releaseNumber ?: 0 >= EtcdVersion.V3_0_11.releaseNumber) {
            assertion.assertTrue(response.hasPrevKv())
            val prevKv = response.prevKv
            assertion.assertEquals(key, prevKv.key)
            assertion.assertEquals(value1, prevKv.value)
        }
        // put with no existing lease
        options = PutOption.newBuilder().withLeaseId(Long.MAX_VALUE).build()
        try {
            kvService.put(key, value2, options)
            assertion.fail("should not execute to here.")
        } catch (expected: Exception) {
            // do nothing
        }
        // TEST_THIS put with existing lease will be test in etcd lease service test
    }

    @Test(dependsOnMethods = arrayOf("testPut"))
    fun testGet() {
        // basic get
        val kvService = etcdClient.newKVService()
        val keyZ1 = baseKeyZ.concat("1".toByteString())
        assertion.assertEquals(keyZ1.toStringUtf8(), "keyZ1")
        kvService.put(keyZ1, value1)
        var getResp = kvService.get(keyZ1)
        assertion.assertEquals(getResp.kvsCount, 1)
        assertion.assertEquals(getResp.getKvs(0).value, value1)
        assertion.assertFalse(getResp.more)
        // serializable get
        getResp = kvService.get(keyZ1, GetOption.newBuilder().withSerializable(true).build())
        assertion.assertEquals(getResp.kvsCount, 1)
        assertion.assertEquals(getResp.getKvs(0).value, value1)
        assertion.assertFalse(getResp.more)
        // get key of revision
        val keyZ2 = baseKeyZ.concat("2".toByteString())
        val putResp = kvService.put(keyZ2, value1)
        kvService.put(keyZ2, value2)
        var getOptions = GetOption.newBuilder().withRevision(putResp.header.revision).build()
        getResp = kvService.get(keyZ2, getOptions)
        assertion.assertEquals(getResp.kvsCount, 1)
        assertion.assertEquals(getResp.getKvs(0).value, value1)

        // get --from-key
        val keyZ3 = baseKeyZ.concat("3".toByteString())
        kvService.put(keyZ3, value)
        getOptions = GetOption.newBuilder()
                .withSortField(RangeRequest.SortTarget.KEY)
                .withSortOrder(RangeRequest.SortOrder.DESCEND)
                .withRange(FROM_KEY)
                .build()
        getResp = kvService.get(keyZ2, getOptions)
        assertion.assertEquals(getResp.kvsCount, 2)
        assertion.assertEquals(getResp.getKvs(0).key, keyZ3)
        assertion.assertEquals(getResp.getKvs(0).value, value)
        assertion.assertEquals(getResp.getKvs(1).key, keyZ2)
        assertion.assertEquals(getResp.getKvs(1).value, value2)

        // get --prefix
        var prefixKey = prefixKeyOf(baseKeyZ)
        getOptions = GetOption.newBuilder()
                .withSortField(RangeRequest.SortTarget.KEY)
                .withSortOrder(RangeRequest.SortOrder.ASCEND)
                .withRange(prefixKey)
                .build()
        getResp = kvService.get(baseKeyZ, getOptions)
        assertion.assertEquals(getResp.kvsCount, 3)
        assertion.assertEquals(getResp.getKvs(0).key, keyZ1)
        assertion.assertEquals(getResp.getKvs(0).value, value1)
        assertion.assertEquals(getResp.getKvs(1).key, keyZ2)
        assertion.assertEquals(getResp.getKvs(1).value, value2)
        assertion.assertEquals(getResp.getKvs(2).key, keyZ3)
        assertion.assertEquals(getResp.getKvs(2).value, value)

        // get --prefix, one bit larger overflow test
        kvService.put("\uffff1".toByteString(), value1)
        kvService.put("\uffff2".toByteString(), value1)
        prefixKey = prefixKeyOf("\uffff")
        getOptions = GetOption.newBuilder()
                .withSortField(RangeRequest.SortTarget.KEY)
                .withSortOrder(RangeRequest.SortOrder.ASCEND)
                .withRange(prefixKey)
                .build()
        getResp = kvService.get("\uffff".toByteString(), getOptions)
        assertion.assertEquals(getResp.kvsCount, 2)

        // all-keys and keys-only
        getOptions = GetOption.newBuilder()
                .withRange(FROM_KEY)
                .withKeysOnly(true)
                .build()
        assertion.assertEquals("\u0000".toByteString(), NULL_KEY)
        getResp = kvService.get(NULL_KEY, getOptions)
        assertion.assertTrue(getResp.kvsCount >= 5)
        assertion.assertEquals(getResp.kvsCount, getResp.count.toInt())
        getResp.kvsList.forEach {
            assertion.assertFalse(it.key.isEmpty)
            assertion.assertTrue(it.value.isEmpty)
        }
        // all-keys but limit (greater than 0)
        getOptions = GetOption.newBuilder()
                .withRange(FROM_KEY)
                .withLimit(2)
                .build()
        getResp = kvService.get(NULL_KEY, getOptions)
        assertion.assertTrue(getResp.kvsCount <= 2)
        assertion.assertTrue(getResp.count >= getResp.kvsCount)
        // all-keys but limit is less than zero
        getOptions = GetOption.newBuilder()
                .withRange(FROM_KEY)
                .withLimit(-2)
                .build()
        getResp = kvService.get(NULL_KEY, getOptions)
        assertion.assertTrue(getResp.kvsCount >= 5)
        assertion.assertEquals(getResp.kvsCount, getResp.count.toInt())
        // all-keys but count-only
        getOptions = GetOption.newBuilder()
                .withRange(FROM_KEY)
                .withCountOnly(true)
                .build()
        getResp = kvService.get(NULL_KEY, getOptions)
        assertion.assertEquals(getResp.kvsCount, 0)
        assertion.assertTrue(getResp.count >= 5)
    }

    @Test(dependsOnMethods = arrayOf("testGet"))
    fun testCommit() {
        val kvService = etcdClient.newKVService()
        assertion.assertNotSame(etcdClient.newKVService(), kvService)
        kvService.put(key, "xyz".toByteString())
        val comparePredicate = Cmp(key, Cmp.CmpOp.GREATER, CmpTarget.value("abc".toByteString()))
        val txnRequestPredicate = Txn.newBuilder()
                .test(comparePredicate)
                .successDo(Op.put(key, "XYZ".toByteString()))
                .failureDo(Op.put(key, "ABC".toByteString()))
                .build()
        val commit = kvService.commit(txnRequestPredicate)

        assertion.assertNotNull(commit.header)
        assertion.assertTrue(commit.succeeded)
        assertion.assertEquals(commit.responsesCount, 1)

        val getResp = kvService.get(key)
        assertion.assertEquals(getResp.kvsCount, 1)
        assertion.assertEquals(getResp.getKvs(0).value.toStringUtf8(), "XYZ")
    }

    @Test(dependsOnMethods = arrayOf("testCommit"))
    fun testCompact() {
        // no options
        val kvService = etcdClient.newKVService()
        try {
            val compactResp = kvService.compact()
            assertion.assertNotNull(compactResp.header)
        } catch (e: Exception) {
            // compact at 0 more than once.
            assertion.assertTrue(e.message?.contains("has been compacted") ?: false)
        }
        // compactRevision
        val lessRevision = kvService.put(key, value).header.revision
        val putResponse = kvService.put(key, "value_new".toByteString())
        val compactOptions = CompactOption.newBuilder().withRevision(putResponse.header.revision).build()
        var compactResp = kvService.compact(compactOptions)
        assertion.assertNotNull(compactResp.header)
        try {
            kvService.get(key, GetOption.newBuilder().withRevision(lessRevision).build())
            assertion.fail()
        } catch (expectAndIgnored: Exception) {
        }
        val revision = kvService.put(key, "what".toByteString()).header.revision
        // withPhysical
        compactResp = kvService.compact(CompactOption.newBuilder().withPhysical(true).withRevision(revision).build())
        assertion.assertNotNull(compactResp.header)
    }

    @Test(dependsOnMethods = arrayOf("testCompact"))
    fun testDelete() {
        // basic delete key
        val kvService = etcdClient.newKVService()
        val shouldDelete = kvService.get(key).count
        var deleteResp = kvService.delete(key)
        assertion.assertEquals(deleteResp.deleted, shouldDelete)
        // delete with prevKV
        var deleteOptions = DeleteOption.newBuilder()
                .withPrevKV(true)
                .build()
        val keyZ1 = baseKeyZ.concat("1".toByteString())
        deleteResp = kvService.delete(keyZ1, deleteOptions)
        assertion.assertEquals(deleteResp.deleted, 1L)
        if (EtcdClient.knowVersion.get()?.releaseNumber ?: 0 >= EtcdVersion.V3_0_11.releaseNumber) {
            assertion.assertEquals(deleteResp.prevKvsCount, 1)
            assertion.assertEquals(deleteResp.getPrevKvs(0).key, keyZ1)
            assertion.assertEquals(deleteResp.getPrevKvs(0).value, value1)
        }
        // delete --from-key
        deleteOptions = DeleteOption.newBuilder()
                .withRange(FROM_KEY)
                .build()
        deleteResp = kvService.delete("\uffff".toByteString(), deleteOptions)
        assertion.assertEquals(deleteResp.deleted, 2L)
        // delete --prefix
        deleteOptions = DeleteOption.newBuilder()
                .withRange(prefixKeyOf(baseKeyZ))
                .build()
        deleteResp = kvService.delete(baseKeyZ, deleteOptions)
        assertion.assertEquals(deleteResp.deleted, 2L)
        // delete all keys
        deleteOptions = DeleteOption.newBuilder().withRange(FROM_KEY).build()
        kvService.delete(NULL_KEY, deleteOptions)
        val getResp = kvService.get(key1, GetOption.newBuilder().withCountOnly(true).withRange(FROM_KEY).build())
        assertion.assertEquals(getResp.count, 0L)
    }

    @Test(dependsOnMethods = arrayOf("testDelete"))
    fun testPutInFuture() {
        val kvService = etcdClient.newKVService()
        // just put with default option
        val response = kvService.putInFuture(key, value).get(5, TimeUnit.SECONDS)
        assertion.assertTrue(response.hasHeader())
        assertion.assertTrue(!response.hasPrevKv())
    }

    @Test(dependsOnMethods = arrayOf("testPutInFuture"))
    fun testGetInFuture() {
        val kvService = etcdClient.newKVService()
        // just get with default option
        val response = kvService.getInFuture(key).get(5, TimeUnit.SECONDS)
        assertion.assertTrue(response.hasHeader())
        assertion.assertEquals(response.count, 1L)
        assertion.assertEquals(response.kvsCount, 1)
        assertion.assertEquals(response.kvsList[0].value, value)
    }

    @Test(dependsOnMethods = arrayOf("testGetInFuture"))
    fun testCommitInFuture() {
        val kvService = etcdClient.newKVService()
        assertion.assertNotSame(etcdClient.newKVService(), kvService)
        kvService.put(key, "xyz".toByteString())
        val comparePredicate = Cmp(key, Cmp.CmpOp.GREATER, CmpTarget.value("abc".toByteString()))
        val txnRequestPredicate = Txn.newBuilder()
                .test(comparePredicate)
                .successDo(Op.put(key, "XYZ".toByteString()))
                .failureDo(Op.put(key, "ABC".toByteString()))
                .build()
        val commit = kvService.commitInFuture(txnRequestPredicate).get(5, TimeUnit.SECONDS)

        assertion.assertNotNull(commit.header)
        assertion.assertTrue(commit.succeeded)
        assertion.assertEquals(commit.responsesCount, 1)

        val getResp = kvService.get(key)
        assertion.assertEquals(getResp.kvsCount, 1)
        assertion.assertEquals(getResp.getKvs(0).value.toStringUtf8(), "XYZ")
    }

    @Test(dependsOnMethods = arrayOf("testCommitInFuture"))
    fun testCompactInFuture() {
        // no options
        val kvService = etcdClient.newKVService()
        try {
            val compactResp = kvService.compact()
            assertion.assertNotNull(compactResp.header)
        } catch (e: Exception) {
            // compact at 0 more than once.
            assertion.assertTrue(e.message?.contains("has been compacted") ?: false)
        }
        // compactRevision
        val lessRevision = kvService.put(key, value).header.revision
        val putResponse = kvService.put(key, "value_new".toByteString())
        val compactOptions = CompactOption.newBuilder().withRevision(putResponse.header.revision).build()
        var compactResp = kvService.compactInFuture(compactOptions).get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(compactResp.header)
        try {
            kvService.get(key, GetOption.newBuilder().withRevision(lessRevision).build())
            assertion.fail()
        } catch (expectAndIgnored: Exception) {
        }
        val revision = kvService.put(key, "what".toByteString()).header.revision
        // withPhysical
        compactResp = kvService.compactInFuture(CompactOption.newBuilder().withPhysical(true).withRevision(revision).build()).get(5, TimeUnit.SECONDS)
        assertion.assertNotNull(compactResp.header)
    }

    @Test(dependsOnMethods = arrayOf("testCompactInFuture"))
    fun testDeleteInFuture() {
        // basic delete key
        val kvService = etcdClient.newKVService()
        val shouldDelete = kvService.get(key).count
        val deleteResp = kvService.deleteInFuture(key).get(5, TimeUnit.SECONDS)
        assertion.assertEquals(deleteResp.deleted, shouldDelete)
        // delete all keys
        val deleteOptions = DeleteOption.newBuilder().withRange(FROM_KEY).build()
        kvService.deleteInFuture(NULL_KEY, deleteOptions).get(5, TimeUnit.SECONDS)
        val getResp = kvService.get(key1, GetOption.newBuilder().withCountOnly(true).withRange(FROM_KEY).build())
        assertion.assertEquals(getResp.count, 0L)
    }

    @Test(dependsOnMethods = arrayOf("testDeleteInFuture"))
    fun testPutAsync() {
        val kvService = etcdClient.newKVService()
        // just put with default option
        val responseRef = AtomicReference<PutResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        kvService.putAsync(key, value, callback = object : ResponseCallback<PutResponse> {
            override fun onResponse(response: PutResponse) {
                responseRef.set(response)
                logger.debug("Put response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                logger.error("", throwable)
            }

            override fun completeCallback() {
                finishLatch.countDown()
                logger.debug("Asynchronously put operation finished.")
            }
        })
        finishLatch.await(5, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertTrue(response.hasHeader())
            assertion.assertTrue(!response.hasPrevKv())
        } else
            throw throwable!!
    }

    @Test(dependsOnMethods = arrayOf("testPutAsync"))
    fun testGetAsync() {
        val kvService = etcdClient.newKVService()
        // just get with default option
        val responseRef = AtomicReference<RangeResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        kvService.getAsync(key, callback = object : ResponseCallback<RangeResponse> {
            override fun onResponse(response: RangeResponse) {
                responseRef.set(response)
                logger.debug("Get range response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                logger.error("", throwable)
            }

            override fun completeCallback() {
                finishLatch.countDown()
                logger.debug("Asynchronously get operation finished.")
            }
        })
        finishLatch.await(5, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertTrue(response.hasHeader())
            assertion.assertEquals(response.count, 1L)
            assertion.assertEquals(response.kvsCount, 1)
            assertion.assertEquals(response.kvsList[0].value, value)
        } else
            throw throwable!!
    }

    @Test(dependsOnMethods = arrayOf("testGetAsync"))
    fun testCommitAsync() {
        val kvService = etcdClient.newKVService()
        assertion.assertNotSame(etcdClient.newKVService(), kvService)
        kvService.put(key, "xyz".toByteString())
        val comparePredicate = Cmp(key, Cmp.CmpOp.GREATER, CmpTarget.value("abc".toByteString()))
        val txn = Txn.newBuilder()
                .test(comparePredicate)
                .successDo(Op.put(key, "XYZ".toByteString()))
                .failureDo(Op.put(key, "ABC".toByteString()))
                .build()
        val responseRef = AtomicReference<TxnResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        kvService.commitAsync(txn, object : ResponseCallback<TxnResponse> {
            override fun onResponse(response: TxnResponse) {
                responseRef.set(response)
                logger.debug("Txn response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                logger.debug("", throwable)
            }

            override fun completeCallback() {
                finishLatch.countDown()
                logger.debug("Asynchronously txn operation finished.")
            }
        })
        finishLatch.await(5, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertNotNull(response.header)
            assertion.assertTrue(response.succeeded)
            assertion.assertEquals(response.responsesCount, 1)
        } else
            throw throwable!!

        val getResp = kvService.get(key)
        assertion.assertEquals(getResp.kvsCount, 1)
        assertion.assertEquals(getResp.getKvs(0).value.toStringUtf8(), "XYZ")
    }

    @Test(dependsOnMethods = arrayOf("testCommitAsync"))
    fun testCompactAsync() {
        val kvService = etcdClient.newKVService()
        // compactRevision
        val lessRevision = kvService.put(key, value).header.revision
        val putResponse = kvService.put(key, "value_new".toByteString())
        val compactOptions = CompactOption.newBuilder().withRevision(putResponse.header.revision).build()
        val responseRef = AtomicReference<CompactionResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        kvService.compactAsync(compactOptions, object : ResponseCallback<CompactionResponse> {
            override fun onResponse(response: CompactionResponse) {
                responseRef.set(response)
                logger.debug("Compact response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                logger.debug("", throwable)
            }

            override fun completeCallback() {
                finishLatch.countDown()
                logger.debug("Asynchronously compact operation finished.")
            }
        })
        finishLatch.await(5, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertNotNull(response.header)
        } else
            throw throwable!!

        try {
            kvService.get(key, GetOption.newBuilder().withRevision(lessRevision).build())
            assertion.fail()
        } catch (expectAndIgnored: Exception) {
        }
    }

    @Test(dependsOnMethods = arrayOf("testCompactAsync"))
    fun testDeleteAsync() {
        val kvService = etcdClient.newKVService()
        // delete all keys
        val deleteOptions = DeleteOption.newBuilder().withRange(FROM_KEY).build()
        val responseRef = AtomicReference<DeleteRangeResponse?>()
        val errorRef = AtomicReference<Throwable?>()
        val finishLatch = CountDownLatch(1)
        kvService.deleteAsync(NULL_KEY, deleteOptions, object : ResponseCallback<DeleteRangeResponse> {
            override fun onResponse(response: DeleteRangeResponse) {
                responseRef.set(response)
                logger.debug("Delete response received.")
            }

            override fun onError(throwable: Throwable) {
                errorRef.set(throwable)
                logger.debug("", throwable)
            }

            override fun completeCallback() {
                finishLatch.countDown()
                logger.debug("Asynchronously delete operation completed.")
            }
        })
        finishLatch.await(5, TimeUnit.SECONDS)
        val response = responseRef.get()
        val throwable = errorRef.get()
        assertion.assertTrue(response != null || throwable != null)
        if (response != null) {
            assertion.assertNotNull(response.header)
        } else
            throw throwable!!

        val getResp = kvService.get(key1, GetOption.newBuilder().withCountOnly(true).withRange(FROM_KEY).build())
        assertion.assertEquals(getResp.count, 0L)
    }
}