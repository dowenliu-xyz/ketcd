package xyz.dowenliu.ketcd.version

/**
 * Known etcd version, since v3.0.0.
 *
 * create at 2017/4/10
 * @author liufl
 * @since 0.1.0
 *
 * @property value Etcd server version value
 */
enum class EtcdVersion(val value: String) {
    /**
     * V3.0.0
     */
    V3_0_0("3.0.0"),
    /**
     * V3.0.1
     */
    V3_0_1("3.0.1"),

    /**
     * V3.0.2
     */
    V3_0_2("3.0.2"),
    /**
     * V3.0.3
     */
    V3_0_3("3.0.3"),
    /**
     * V3.0.4
     */
    V3_0_4("3.0.4"),
    /**
     * V3.0.5
     */
    V3_0_5("3.0.5"),
    /**
     * V3.0.6
     */
    V3_0_6("3.0.6"),
    /**
     * V3.0.7
     */
    V3_0_7("3.0.7"),
    /**
     * V3.0.8
     */
    V3_0_8("3.0.8"),
    /**
     * V3.0.9
     */
    V3_0_9("3.0.9"),
    /**
     * V3.0.10
     */
    V3_0_10("3.0.10"),
    /**
     * V3.0.11
     */
    V3_0_11("3.0.11"),
    /**
     * V3.0.12
     */
    V3_0_12("3.0.12"),
    /**
     * V3.0.13
     */
    V3_0_13("3.0.13"),
    /**
     * V3.0.14
     */
    V3_0_14("3.0.14"),
    /**
     * V3.0.15
     */
    V3_0_15("3.0.15"),
    /**
     * V3.0.16
     */
    V3_0_16("3.0.16"),
    /**
     * V3.0.17
     */
    V3_0_17("3.0.17"),
    /**
     * V3.1.0-alpha.0
     */
    V3_1_0_alpha0("3.1.0-alpha.0"),
    /**
     * V3.1.0-alpha.1
     */
    V3_1_0_alpha1("3.1.0-alpha.1"),
    /**
     * V3.1.0-rc.0
     */
    V3_1_0_rc0("3.1.0-rc.0"),
    /**
     * V3.1.0-rc.0
     */
    V3_1_0_rc1("3.1.0-rc.1"),
    /**
     * v3.1.0
     */
    V3_1_0("3.1.0"),
    /**
     * v3.1.1
     */
    V3_1_1("3.1.1"),
    /**
     * v3.1.2
     */
    V3_1_2("3.1.2"),
    /**
     * v3.1.3
     */
    V3_1_3("3.1.3"),
    /**
     * v3.1.4
     */
    V3_1_4("3.1.4"),
    /**
     * v3.1.5
     */
    V3_1_5("3.1.5"),
    /**
     * v3.1.6
     */
    V3_1_6("3.1.6"),
    /**
     * v3.1.7
     */
    V3_1_7("3.1.7"),
    /**
     * v3.1.8
     */
    V3_1_8("3.1.8"),
    /**
     * v3.1.9
     */
    V3_1_9("3.1.9"),
    /**
     * v3.2.0-rc.0
     */
    V3_2_0_rc0("3.2.0-rc.0"),
    /**
     * v3.2.0-rc.1
     */
    V3_2_0_rc1("3.2.0-rc.1"),
    /**
     * v3.2.0
     */
    V3_2_0("3.2.0"),
    /**
     * v3.2.1
     */
    V3_2_1("3.2.1"),
    /**
     * v3.2.2
     */
    V3_2_2("3.2.2")
    ;

    /**
     * Companion object of [EtcdVersion]
     */
    companion object {
        /**
         * Current latest etcd version.
         */
        @JvmStatic val CURRENT = V3_2_2

        private val valueMap = EtcdVersion.values().map { Pair(it.value, it) }.toMap()

        /**
         * Get enum value of real release version string.
         */
        @JvmStatic fun ofValue(value: String): EtcdVersion? = valueMap[value]
    }
}