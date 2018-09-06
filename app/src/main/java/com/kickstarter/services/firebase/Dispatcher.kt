package com.kickstarter.services.firebase

import android.content.Context
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver

const val REGISTER_SERVICE = "Register-service"
const val UNREGISTER_SERVICE = "Unregister-service"

fun dispatchTokenJob(context: Context, jobName: String) {
    val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
    val job = dispatcher.newJobBuilder()
            .setService(RegisterService::class.java)
            .setTag(jobName)
            .build()

    dispatcher.mustSchedule(job)
}
