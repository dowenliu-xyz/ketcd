package xyz.dowenliu.ketcd.kv

import xyz.dowenliu.ketcd.api.TxnRequest

/**
 * Build an etcd transaction.
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @property testList The if conditions of a transaction.
 * @property successOpList The if operations of a transaction.
 * @property failureOpList The else operations of a transaction.
 */
class Txn private constructor(private val testList: List<Cmp>,
                              private val successOpList: List<Op>,
                              private val failureOpList: List<Op>) {
    /**
     * Companion object of [Txn]
     */
    companion object {
        /**
         * Get a [Builder].
         */
        fun newBuilder(): Builder = Builder()
    }

    /**
     * Builder to build a [Txn].
     */
    class Builder internal constructor() {
        private var testList: List<Cmp> = emptyList()
        private var successOpList: List<Op> = emptyList()
        private var failureOpList: List<Op> = emptyList()

        /**
         * Set transaction if condition.
         *
         * @param tests The if tests.
         */
        fun test(vararg tests: Cmp): Builder {
            testList = tests.toList()
            return this
        }

        /**
         * Set transaction if operations.
         *
         * @param ops The operations to do if the if condition test success.
         */
        fun successDo(vararg ops: Op): Builder {
            successOpList = ops.toList()
            return this
        }

        /**
         * Set transaction else operations.
         *
         * @param ops The operations to do if the if condition test failure.
         */
        fun failureDo(vararg ops: Op): Builder {
            failureOpList = ops.toList()
            return this
        }

        /**
         * Build a [Txn].
         */
        fun build(): Txn = Txn(testList, successOpList, failureOpList)
    }

    /**
     * Predicate a [TxnRequest] using in the deeper gRPC APIs.
     */
    fun toTxnRequest(): TxnRequest =
            TxnRequest.newBuilder()
                    .addAllCompare(testList.map(Cmp::toCompare))
                    .addAllSuccess(successOpList.map(Op::toRequestOp))
                    .addAllFailure(failureOpList.map(Op::toRequestOp))
                    .build()
}