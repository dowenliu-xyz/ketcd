package xyz.dowenliu.ketcd.version

import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

/**
 * Annotate the etcd version that the function, field or type works with.
 *
 * create at 2017/4/10
 * @author liufl
 * @since 0.1.0
 *
 * @property since annotate version scope start.
 * @property until annotate version scope end.
 */
@MustBeDocumented
@Retention(BINARY)
@Target(CLASS, FUNCTION, FIELD)
annotation class ForEtcdVersion(val since: EtcdVersion, val until: EtcdVersion = EtcdVersion.V3_2_2)