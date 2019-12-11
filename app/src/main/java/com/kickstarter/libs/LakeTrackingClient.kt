package com.kickstarter.libs

import android.content.Context
import com.firebase.jobdispatcher.JobService
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.utils.MapUtils
import com.kickstarter.models.User
import com.kickstarter.services.LakeBackgroundService
import com.kickstarter.ui.IntentKey
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LakeTrackingClient(
        @param:ApplicationContext private val context: Context,
        currentUser: CurrentUserType,
        build: Build,
        currentConfig: CurrentConfigType) : TrackingClient(context, currentUser, build, currentConfig) {
    private var loggedInUser: User? = null
    private var config: Config? = null

    init {

        // Cache the most recent logged in user for default Lake properties.
        this.currentUser.observable().subscribe { u -> this.loggedInUser = u }

        // Cache the most recent config for default Lake properties.
        this.currentConfig.observable().subscribe { c -> this.config = c }
    }

    override fun backgroundServiceClass(): Class<out JobService> = LakeBackgroundService::class.java

    override fun cleanPropertiesOnly(): Boolean = true

    override fun eventKey(): String = IntentKey.LAKE_EVENT

    override fun eventNameKey(): String = IntentKey.LAKE_EVENT_NAME

    override fun tag(): String = LakeTrackingClient::class.java.simpleName

    @Throws(JSONException::class)
    override fun trackingData(eventName: String, newProperties: Map<String, Any?>): String {
        val data = JSONObject()
        data.put("event", eventName)

        val compactProperties = MapUtils.compact(newProperties)
        val propertiesJSON = JSONObject()
        for ((key, value) in compactProperties) {
            propertiesJSON.put(key, value)
        }
        data.put("properties", propertiesJSON)

        val record = JSONObject()
        record.put("partition-key", UUID.randomUUID().toString())
        record.put("data", data)
        return record.toString()
    }

}
