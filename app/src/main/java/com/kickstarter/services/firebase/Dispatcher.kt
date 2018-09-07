package com.kickstarter.services.firebase

import android.content.Context
import com.firebase.jobdispatcher.*

fun dispatchJob(context: Context, serviceClas: Class<out JobService>, jobName: String) {
    val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
    val job = dispatcher.newJobBuilder()
            .setConstraints(Constraint.ON_ANY_NETWORK)
            .setService(serviceClas)
            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
            .setTag(jobName)
            .build()

    dispatcher.mustSchedule(job)
}

