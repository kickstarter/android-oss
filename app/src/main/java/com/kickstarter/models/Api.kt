package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Api private constructor(
    private val project: String?,
    private val comments: String?,
    private val updates: String?
) : Parcelable {
    fun project() = this.project
    fun comments() = this.comments
    fun updates() = this.updates

    @Parcelize
    data class Builder(
        private var project: String? = null,
        private var comments: String? = null,
        private var updates: String? = null
    ) : Parcelable {
        fun project(project: String?) = apply { this.project = project }
        fun comments(comments: String?) = apply { this.comments = comments }
        fun updates(updates: String?) = apply { this.updates = updates }
        fun build() = Api(
            project = project,
            comments = comments,
            updates = updates
        )
    }

    fun toBuilder() = Builder(
        project = project,
        comments = comments,
        updates = updates
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Api) {
            equals = project() == obj.project() &&
                comments() == obj.comments() &&
                updates() == obj.updates()
        }
        return equals
    }
}
