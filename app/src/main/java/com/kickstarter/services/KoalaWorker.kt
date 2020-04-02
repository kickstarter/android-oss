package com.kickstarter.services

import android.content.Context
import androidx.work.WorkerParameters
import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.KSApplication
import com.kickstarter.libs.qualifiers.ApplicationContext
import rx.schedulers.Schedulers
import javax.inject.Inject

class KoalaWorker(@ApplicationContext applicationContext: Context, params: WorkerParameters) : TrackingWorker(applicationContext, params) {
    @Inject
    lateinit var koala: KoalaService

    override fun doWork(): Result {
        (applicationContext as KSApplication).component().inject(this)
        val encodedData = Base64Utils.encodeUrlSafe(eventData.toByteArray())
        val result = this.koala
                .track(encodedData)
                .subscribeOn(Schedulers.io())
                .toBlocking()
                .first()
        return handleResult(result)
    }
}
