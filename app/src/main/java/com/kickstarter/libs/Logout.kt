package com.kickstarter.libs

import com.facebook.login.LoginManager
import java.net.CookieManager

class Logout(private val cookieManager: CookieManager, private val currentUser: CurrentUserType) {
    fun execute() {
        currentUser.logout()
        cookieManager.cookieStore.removeAll()
        LoginManager.getInstance().logOut()
    }
}
