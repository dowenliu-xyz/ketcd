package xyz.dowenliu.ketcd.option

/**
 * The options for compaction operation.
 *
 * create at 2017/4/15
 * @author liufl
 * @since 0.1.0
 *
 * @property revision The revision to use for the compact request.
 * @property physical Whether the compact should wait until physically applied
 */
class CompactOption private constructor(val revision: Long, val physical: Boolean) {
    /**
     * Companion object of [CompactOption]
     */
    companion object {
        /**
         * The default compact options.
         */
        @JvmStatic val DEFAULT = newBuilder().build()

        /**
         * Create a builder to construct options for compaction operation.
         *
         * @return builder
         */
        @JvmStatic fun newBuilder() = Builder()
    }

    /**
     * Builder to construct [CompactOption].
     */
    class Builder  internal constructor() {
        private var revision = 0L
        private var physical = false

        /**
         * Provide the revision to use for the compact request.
         *
         * All superseded keys with a revision less than the compaction revision will be removed.
         *
         * @param revision the revision to compact
         * @return this builder to train.
         */
        fun withRevision(revision: Long): Builder {
            this.revision = revision
            return this
        }

        /**
         * Set the compact request to wait until the compaction is physically applied.
         *
         * @param physical whether the compact should wait until physically applied
         * @return this builder to train.
         */
        fun withPhysical(physical: Boolean): Builder {
            this.physical = physical
            return this
        }

        /**
         * Construct a [CompactOption].
         */
        fun build(): CompactOption = CompactOption(revision, physical)
    }
}