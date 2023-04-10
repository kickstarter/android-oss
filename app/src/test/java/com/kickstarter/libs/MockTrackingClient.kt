package com.kickstarter.libs

import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.extensions.currentVariants
import com.kickstarter.mock.factories.ConfigFactory.config
import com.kickstarter.models.User
import org.joda.time.DateTime
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class MockTrackingClient(
    currentUser: CurrentUserType,
    currentConfig: CurrentConfigType,
    private val type: Type,
    private val ffClient: FeatureFlagClientType
) : TrackingClientType() {
    override var loggedInUser: User? = null
    override var config: Config? = config()
    override var isInitialized = false
    private fun propagateUser(user: User?) {
        user?.let { identify(it) }
        loggedInUser = user
    }

    override fun identify(user: User) {
        identifiedUser.onNext(user)
    }

    override fun reset() {
        loggedInUser = null
        identifiedUser.onNext(null)
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun initialize() {
        this.isInitialized = true
    }

    class Event internal constructor(val name: String, val properties: Map<String, Any>)

    private val events = PublishSubject.create<Event>()
    val eventNames = events.map { e: Event -> e.name }
    val eventProperties = events.map { e: Event -> e.properties }
    val identifiedUser = BehaviorSubject.create<User?>()

    init {
        currentUser.observable().subscribe { user: User? -> propagateUser(user) }
        currentConfig.observable().subscribe { c: Config? -> config = c }
    }

    override fun track(eventName: String, additionalProperties: Map<String, Any>) {
        events.onNext(Event(eventName, combinedProperties(additionalProperties)))
    }

    override fun type(): Type {
        return type
    }

    // Default property values
    override fun brand(): String {
        return "Google"
    }

    override fun buildNumber(): Int {
        return 9999
    }

    override fun currentVariants(): Array<String>? {
        return config!!.currentVariants()
    }

    override fun deviceDistinctId(): String {
        return "uuid"
    }

    override fun deviceFormat(): String {
        return "phone"
    }

    override fun deviceOrientation(): String {
        return "portrait"
    }

    override val isGooglePlayServicesAvailable: Boolean
        protected get() = false
    override val isTalkBackOn: Boolean
        protected get() = false

    override fun manufacturer(): String {
        return "Google"
    }

    override fun model(): String {
        return "Pixel 3"
    }

    override fun OSVersion(): String {
        return "9"
    }

    override fun time(): Long {
        return DEFAULT_TIME
    }

    override fun loggedInUser(): User? {
        return loggedInUser
    }

    override fun userAgent(): String? {
        return "agent"
    }

    override fun userCountry(user: User): String {
        val location = user.location()
        val configCountry = if (config != null) config!!.countryCode() else null
        return location?.country() ?: configCountry!!
    }

    override fun sessionCountry(): String {
        return "US"
    }

    override fun versionName(): String {
        return "9.9.9"
    }

    override fun wifiConnection(): Boolean {
        return false
    }

    companion object {
        private val DEFAULT_TIME = DateTime.parse("2018-11-02T18:42:05Z").millis / 1000
    }
}
