package com.kickstarter.models

enum class QualtricsIntercept(private val prodId: String, private val testId: String) {
    NATIVE_APP_FEEDBACK("SI_3VjP7wWiUZg2wBf", "SI_6nSwomRDiWXeXEV");

    fun id(packageName: String) :String {
        return when (packageName){
             "com.kickstarter.kickstarter" -> prodId
            else -> testId
        }
    }
}
