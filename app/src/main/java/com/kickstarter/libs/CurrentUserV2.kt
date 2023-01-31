package com.kickstarter.libs

import com.google.gson.Gson
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

abstract class CurrentUserTypeV2 {
    /**
     * Call when a user has logged in. The implementation of `CurrentUserType` is responsible
     * for persisting the user and access token.
     */
    abstract fun login(newUser: User, accessToken: String)

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
        return observable().filter {
            ObjectUtils.isNotNull(
                it.getValue()
            )
        }.map { it.getValue() }
    }
}

class CurrentUserV2(
    private val accessTokenPreference: StringPreferenceType,
    private val deviceRegistrar: DeviceRegistrarType,
    gson: Gson,
    private val userPreference: StringPreferenceType
) : CurrentUserTypeV2() {
    val user = BehaviorSubject.create<KsOptional<User>>()
    init {
        user
            .map { it.getValue() }
            .skip(1)
            .filter { `object`: User? -> ObjectUtils.isNotNull(`object`) }
            .subscribe { u: User? ->
                userPreference.set(gson.toJson(u, User::class.java))
            }.dispose()

        if (gson.fromJson(userPreference.get(), User::class.java) != null) {
            user.onNext(KsOptional.of(gson.fromJson(userPreference.get(), User::class.java)))
        } else {
            user.onNext(KsOptional.empty())
        }
    }

    override fun getUser(): User? {
        return user.value?.getValue()
    }

    override fun exists(): Boolean {
        return getUser() != null
    }

    override val accessToken: String?
        get() = accessTokenPreference.get()

    override fun login(newUser: User, accessToken: String) {
        Timber.d("Login user %s", newUser.name())
        accessTokenPreference.set(accessToken)
        user.onNext(KsOptional.of(newUser))
        deviceRegistrar.registerDevice()
    }

    override fun logout() {
        Timber.d("Logout current user")
        userPreference.delete()
        accessTokenPreference.delete()
        //  user.onNext(null)
        deviceRegistrar.unregisterDevice()
    }

    override fun refresh(freshUser: User) {
        user.onNext(KsOptional.of(freshUser))
    }

    override fun observable(): Observable<KsOptional<User>> {
        return user
    }
}
