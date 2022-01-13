package com.kickstarter.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Urls private constructor(
    private val web: Web,
    private val api: Api?
) : Parcelable {
    fun web() = this.web
    fun api() = this.api

    @Parcelize
    data class Builder(
        private var web: Web = Web.builder().build(),
        private var api: Api? = null
    ) : Parcelable {
        fun web(web: Web?) = apply { this.web = web ?: Web.builder().build() }
        fun api(api: Api?) = apply { this.api = api }
        fun build() = Urls(
            web = web,
            api = api
        )
    }

    fun toBuilder() = Builder(
        web = web,
        api = api
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Urls) {
            equals = web() == obj.web() &&
                api() == obj.api()
        }
        return equals
    }
}
