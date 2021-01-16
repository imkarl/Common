package cn.imkarl.core.common.json

/**
 * 不进行序列化
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreSerialize
