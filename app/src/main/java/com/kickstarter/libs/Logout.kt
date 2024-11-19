package com.kickstarter.libs

import com.facebook.login.LoginManager
import java.net.CookieManager

class Logout(
    private val cookieManager: CookieManager,
    private val currentUserV2: CurrentUserTypeV2
) {
    fun execute() {
        currentUserV2.logout()
        cookieManager.cookieStore.removeAll()
        LoginManager.getInstance().logOut()
    }
}
