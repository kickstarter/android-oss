package com.kickstarter.libs

import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.models.User
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class MockCurrentUserV2 : CurrentUserTypeV2 {
    private val user = BehaviorSubject.create<KsOptional<User>>()
    private var accessTokenPref: String? = null

    constructor() {
        user.onNext(KsOptional.empty())
    }

    constructor(initialUser: User) {
        user.onNext(KsOptional.of(initialUser))
    }

    override fun setToken(accessToken: String) {
        this.accessTokenPref = accessToken
    }
    override fun login(newUser: User) {
        user.onNext(KsOptional.of(newUser))
    }

    override fun logout() {
        user.onNext(KsOptional.empty())
        accessTokenPref = null
    }

    override val accessToken: String?
        get() = accessTokenPref

    override fun refresh(freshUser: User) {
        user.onNext(KsOptional.of(freshUser))
    }

    override fun observable(): Observable<KsOptional<User>> {
        return user
    }

    override fun getUser(): User? {
        return user.value?.getValue()
    }
}
