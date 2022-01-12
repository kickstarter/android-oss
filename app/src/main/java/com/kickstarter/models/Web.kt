package com.kickstarter.models

import android.os.Parcelable
import com.kickstarter.libs.utils.UrlUtils
import kotlinx.parcelize.Parcelize

@Parcelize
class Web private constructor(
    private val project: String,
    private val projectShort: String?,
    private val rewards: String?,
    private val updates: String?
) : Parcelable {
    fun project() = this.project
    fun projectShort() = this.projectShort
    fun rewards() = this.rewards
    fun updates() = this.updates

    @Parcelize
    data class Builder(
        private var project: String = "",
        private var projectShort: String? = null,
        private var rewards: String? = null,
        private var updates: String? = null
    ) : Parcelable {
        fun project(project: String?) = apply { this.project = project ?: "" }
        fun projectShort(projectShort: String?) = apply { this.projectShort = projectShort }
        fun rewards(rewards: String?) = apply { this.rewards = rewards }
        fun updates(updates: String?) = apply { this.updates = updates }
        fun build() = Web(
            project = project,
            projectShort = projectShort,
            rewards = rewards,
            updates = updates
        )
    }

    fun toBuilder() = Builder(
        project = project,
        projectShort = projectShort,
        rewards = rewards,
        updates = updates
    )

    fun creatorBio() = UrlUtils.appendPath(project(), "creator_bio")

    fun description() = UrlUtils.appendPath(project(), "description")

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Web) {
            equals = project() == obj.project() &&
                projectShort() == obj.projectShort() &&
                rewards() == obj.rewards() &&
                updates() == obj.updates()
        }
        return equals
    }
}
