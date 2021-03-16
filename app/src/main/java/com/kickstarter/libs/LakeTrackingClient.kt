package com.kickstarter.libs

import android.content.Context
import androidx.work.*
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.MapUtils
import com.kickstarter.libs.utils.WorkUtils
import com.kickstarter.models.User
import com.kickstarter.services.LakeWorker
import com.kickstarter.ui.IntentKey
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LakeTrackingClient(
    @param:ApplicationContext private val context: Context,
    currentUser: CurrentUserType,
    build: Build,
    currentConfig: CurrentConfigType,
    optimizely: ExperimentsClientType
) : TrackingClient(context, currentUser, build, currentConfig, optimizely) {
    private var loggedInUser: User? = null
    private var config: Config? = null

    init {
        // Cache the most recent config for default Lake properties.
        this.currentConfig.observable().subscribe { c -> this.config = c }
    }

    override fun type() = Type.LAKE

    @Throws(JSONException::class)
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>) {
        val data = JSONObject()
        data.put("event", eventName)

        val compactProperties = MapUtils.compact(newProperties)
        val propertiesJSON = JSONObject()
        for ((key, value) in compactProperties) {
            propertiesJSON.put(key, value)
        }
        data.put("properties", propertiesJSON)

        val record = JSONObject()
        record.put("partition-key", this.loggedInUser?.id()?.toString() ?: deviceDistinctId())
        record.put("data", data)

        callLakeServiceWithEvent(eventName, record.toString())
    }

    /**
     * Specific for DataLake we send the the event data to a concrete endpoint
     * using the LakeWorker.
     */
    private fun callLakeServiceWithEvent(eventName: String, data: String) {

        val data = workDataOf(
            IntentKey.TRACKING_CLIENT_TYPE_TAG to type().tag,
            IntentKey.EVENT_NAME to eventName,
            IntentKey.EVENT_DATA to data
        )

        val requestBuilder = OneTimeWorkRequestBuilder<LakeWorker>()
        requestBuilder.apply {
            val request = this
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .setConstraints(WorkUtils.baseConstraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WorkUtils.uniqueWorkName(type().tag), ExistingWorkPolicy.APPEND, request)
        }
    }
}
