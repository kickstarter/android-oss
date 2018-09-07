package com.kickstarter.services.firebase

import android.content.Context
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.JobService

fun dispatchJob(context: Context, serviceClas: Class<out JobService>, jobName: String) {
    val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
    val job = dispatcher.newJobBuilder()
            .setConstraints(Constraint.ON_ANY_NETWORK)
            .setService(serviceClas)
            .setTag(jobName)
            .build()

    dispatcher.mustSchedule(job)
}

