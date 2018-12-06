package com.kickstarter.services.firebase

import android.content.Context
import android.os.Bundle
import com.firebase.jobdispatcher.*

@JvmOverloads
fun dispatchJob(context: Context, serviceClass: Class<out JobService>, uniqueJobName: String, extras: Bundle = Bundle()) {
    val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
    val job = dispatcher.newJobBuilder()
            .setConstraints(Constraint.ON_ANY_NETWORK)
            .setService(serviceClass)
            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
            .setExtras(extras)
            .setTag(uniqueJobName)
            .build()

    dispatcher.mustSchedule(job)
}
