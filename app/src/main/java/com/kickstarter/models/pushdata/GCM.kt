package com.kickstarter.models.pushdata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class GCM private constructor(
    private val alert: String,
    private val title: String
) : Parcelable {
    fun alert() = this.alert
    fun title() = this.title

    @Parcelize
    data class Builder(
        private var alert: String = "",
        private var title: String = ""
    ) : Parcelable {
        fun alert(alert: String) = apply { this.alert = alert }
        fun title(title: String) = apply { this.title = title }

        fun build() = GCM(
            alert = alert,
            title = title
        )
    }

    fun toBuilder() = Builder(
        alert = alert,
        title = title
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is GCM) {
            equals = alert() == other.alert() &&
                title() == other.title()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
