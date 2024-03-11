package com.kickstarter.ui.data

import android.net.Uri
import android.os.Parcelable
import com.kickstarter.libs.RefTag
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User
import kotlinx.parcelize.Parcelize

/**
 * A light-weight value to hold two ref tags, the full deeplink, and a project.
 * Two ref tags are stored: one comes from parceled data in the activity
 * and the other comes from the ref stored in a cookie associated to the project.
 */

@Parcelize
class ProjectData private constructor(
    private val refTagFromIntent: RefTag?,
    private val refTagFromCookie: RefTag?,
    private val fullDeeplink: Uri?,
    private val project: Project,
    private val backing: Backing?,
    private val user: User?,
) : Parcelable {
    fun refTagFromIntent() = this.refTagFromIntent
    fun refTagFromCookie() = this.refTagFromCookie
    fun fullDeeplink() = this.fullDeeplink
    fun project() = this.project
    fun backing() = this.backing
    fun user() = this.user

    @Parcelize
    data class Builder(
        private var refTagFromIntent: RefTag? = null,
        private var refTagFromCookie: RefTag? = null,
        private var fullDeeplink: Uri? = null,
        private var project: Project = Project.builder().build(),
        private var backing: Backing? = null,
        private var user: User? = null,

    ) : Parcelable {
        fun refTagFromIntent(refTagFromIntent: RefTag?) = apply { this.refTagFromIntent = refTagFromIntent }
        fun refTagFromCookie(refTagFromCookie: RefTag?) = apply { this.refTagFromCookie = refTagFromCookie }
        fun fullDeeplink(fullDeeplink: Uri?) = apply { this.fullDeeplink = fullDeeplink }
        fun project(project: Project) = apply { this.project = project }
        fun backing(backing: Backing) = apply { this.backing = backing }
        fun user(user: User) = apply { this.user = user }
        fun build() = ProjectData(
            refTagFromIntent = refTagFromIntent,
            refTagFromCookie = refTagFromCookie,
            fullDeeplink = fullDeeplink,
            project = project,
            backing = backing,
            user = user
        )
    }

    fun toBuilder() = Builder(
        refTagFromIntent = refTagFromIntent,
        refTagFromCookie = refTagFromCookie,
        fullDeeplink = fullDeeplink,
        project = project,
        backing = backing,
        user = user
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is ProjectData) {
            equals = refTagFromCookie() == other.refTagFromCookie() &&
                refTagFromIntent() == other.refTagFromIntent() &&
                project() == other.project() &&
                backing() == other.backing() &&
                user() == other.user()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
