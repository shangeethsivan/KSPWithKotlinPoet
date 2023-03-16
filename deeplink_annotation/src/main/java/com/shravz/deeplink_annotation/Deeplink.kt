package com.shravz.deeplink_annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Deeplink(
    val domain: String,
    val navigationRoute: String
)