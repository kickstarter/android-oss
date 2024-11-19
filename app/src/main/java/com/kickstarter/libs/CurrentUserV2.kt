package com.kickstarter.libs

import com.google.gson.Gson
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

abstract class CurrentUserTypeV2 {

    /***
     * Persist a new token, retrieved form #exchange endpoint {/v1/oauth/authorizations/exchange}
     */
    abstract fun setToken(accessToken: String)

    /**
     * Call when a user has logged in. The implementation of `CurrentUserType` is responsible
     * for persisting the user.
     */
    abstract fun login(newUser: User)

    /**
     * Call when a user should be logged out.
     */
    abstract fun logout()

    /**
     * Get the logged in user's access token.
     */
    abstract val accessToken: String?

    /**
     * Updates the persisted current user with a fresh, new user.
     */
    abstract fun refresh(freshUser: User)

    /**
     * Returns an observable representing the current user. It emits immediately
     * with the current user, and then again each time the user is updated.
     */
    abstract fun observable(): Observable<KsOptional<User>>

    /**
     * Returns the most recently emitted user from the user observable.
     */
    @Deprecated("Prefer {@link #observable()}")
    abstract fun getUser(): User?

    /**
     * Returns a boolean that determines if there is a currently logged in user or not.
     */
    @Deprecated("Prefer {@link #observable()}")
    open fun exists(): Boolean {
        return getUser() != null
    }

    /**
     * Emits a boolean that determines if the user is logged in or not. The returned
     * observable will emit immediately with the logged in state, and then again
     * each time the current user is updated.
     */
    val isLoggedIn: Observable<Boolean>
        get() = observable().map {
            it.isPresent()
        }

    /**
     * Emits only values of a logged in user. The returned observable may never emit.
     */
    fun loggedInUser(): Observable<User> {
        return observable()
            .filter { it.getValue().isNotNull() }
            .map { it.getValue() }
    }
}

class CurrentUserV2(
    private val accessTokenPreference: StringPreferenceType,
    private val deviceRegistrar: DeviceRegistrarType,
    private val gson: Gson,
    private val userPreference: StringPreferenceType
) : CurrentUserTypeV2() {
    private val user = BehaviorSubject.create<KsOptional<User>>()

    init {
        val persistedUser = gson.fromJson(userPreference.get(), User::class.java)
        if (persistedUser != null) {
            user.onNext(KsOptional.of(persistedUser))
        } else {
            user.onNext(KsOptional.empty())
        }

        Timber.d("${this.javaClass} init persisted User: $persistedUser")
    }

    override fun getUser(): User? {
        return user.value?.getValue()
    }

    override fun exists(): Boolean {
        return getUser() != null
    }

    override val accessToken: String?
        get() = accessTokenPreference.get()

    override fun login(newUser: User) {
        user.onNext(KsOptional.of(newUser))
        userPreference.set(gson.toJson(newUser, User::class.java))

        Timber.d("${this.javaClass} Login user %s", newUser.name())
    }

    override fun setToken(accessToken: String) {
        // - Clean previous token in case there is any
        accessTokenPreference.delete()
        deviceRegistrar.unregisterDevice()

        // - Register new token
        accessTokenPreference.set(accessToken)
        deviceRegistrar.registerDevice()

        Timber.d("${this.javaClass} setToken: $accessToken")
    }

    override fun logout() {
        accessTokenPreference.delete()
        user.onNext(KsOptional.empty())
        userPreference.delete()
        deviceRegistrar.unregisterDevice()
        Timber.d("${this.javaClass} Logout current user")
    }

    override fun refresh(freshUser: User) {
        user.onNext(KsOptional.of(freshUser))
        userPreference.set(gson.toJson(freshUser, User::class.java))
        Timber.d("${this.javaClass} Refresh current user")
    }

    override fun observable(): Observable<KsOptional<User>> {
        return user
    }
}
