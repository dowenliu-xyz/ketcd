package xyz.dowenliu.ketcd.version

/**
 * Known etcd version, since v3.0.0.
 *
 * create at 2017/4/10
 * @author liufl
 * @since 0.1.0
 *
 * @property value Etcd server version value
 * @property releaseNumber tag version is the Xth release, e.g. v3.0.0 is the 1st release for etcd v3
 */
enum class EtcdVersion(val value: String, val releaseNumber: Int) {
    V3_0_0("3.0.0", 0),
    V3_0_1("3.0.1", 1),
    V3_0_2("3.0.2", 2),
    V3_0_3("3.0.3", 3),
    V3_0_4("3.0.4", 4),
    V3_0_5("3.0.5", 5),
    V3_0_6("3.0.6", 6),
    V3_0_7("3.0.7", 7),
    V3_0_8("3.0.8", 8),
    V3_0_9("3.0.9", 9),
    V3_0_10("3.0.10", 10),
    V3_0_11("3.0.11", 11),
    V3_0_12("3.0.12", 12),
    V3_0_13("3.0.13", 13),
    V3_0_14("3.0.14", 14),
    V3_0_15("3.0.15", 15),
    V3_0_16("3.0.16", 16),
    V3_0_17("3.0.17", 17),
    V3_1_0_alpha0("3.1.0-alpha.0", 18),
    V3_1_0_alpha1("3.1.0-alpha.1", 19),
    V3_1_0_rc0("3.1.0-rc.0", 20),
    V3_1_0_rc1("3.1.0-rc.1", 21),
    V3_1_0("3.1.0", 22),
    V3_1_1("3.1.1", 23),
    V3_1_2("3.1.2", 24),
    V3_1_3("3.1.3", 25),
    V3_1_4("3.1.4", 26),
    V3_1_5("3.1.5", 27),
    V3_1_6("3.1.6", 28),
    ;

    companion object {
        /**
         * Current latest etcd version.
         */
        @JvmStatic val CURRENT = V3_1_6

        private val valueMap = EtcdVersion.values().map { Pair(it.value, it) }.toMap()

        /**
         * Get enum value of real release version string.
         */
        @JvmStatic fun ofValue(value: String): EtcdVersion? = valueMap[value]
    }
}