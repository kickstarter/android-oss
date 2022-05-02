package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.Project
import com.kickstarter.models.User
import kotlinx.parcelize.Parcelize

@Parcelize
class StarEnvelope private constructor(
    private val project: Project,
    private val user: User
) : Parcelable {
    fun project() = this.project
    fun user() = this.user

    @Parcelize
    data class Builder(
        private var project: Project = Project.builder().build(),
        private var user: User = User.builder().build()
    ) : Parcelable {
        fun project(project: Project) = apply { this.project = project }
        fun user(user: User) = apply { this.user = user }
        fun build() = StarEnvelope(
            project = project,
            user = user
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is StarEnvelope) {
            equals = project() == obj.project() &&
                user() == obj.user()
        }
        return equals
    }

    fun toBuilder() = Builder(
        project = project,
        user = user
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
