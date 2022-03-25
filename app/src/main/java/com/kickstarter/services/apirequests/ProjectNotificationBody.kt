package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ProjectNotificationBody private constructor(
    private val email: Boolean,
    private val mobile: Boolean
) :
    Parcelable {
    fun email() = this.email
    fun mobile() = this.mobile

    @Parcelize
    data class Builder(
        private var email: Boolean = false,
        private var mobile: Boolean = false,
    ) : Parcelable {
        fun email(email: Boolean) = apply { this.email = email }
        fun mobile(mobile: Boolean) = apply { this.mobile = mobile }

        fun build() = ProjectNotificationBody(
            email = email,
            mobile = mobile
        )
    }

    fun toBuilder() = Builder(
        email = email,
        mobile = mobile
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is ProjectNotificationBody) {
            equals = email() == obj.email() &&
                mobile() == obj.mobile()
        }
        return equals
    }
}
