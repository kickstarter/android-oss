package com.kickstarter.ui.data

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.libs.RefTag
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User

/**
 * A light-weight value to hold two ref tags and a project.
 * Two ref tags are stored: one comes from parceled data in the activity
 * and the other comes from the ref stored in a cookie associated to the project.
 */

@AutoParcel
abstract class ProjectData : Parcelable {
    abstract fun refTagFromIntent(): RefTag?
    abstract fun refTagFromCookie(): RefTag?
    abstract fun project(): Project
    abstract fun backing(): Backing?
    abstract fun user(): User?

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun refTagFromIntent(intentRefTag: RefTag?): Builder
        abstract fun refTagFromCookie(cookieRefTag: RefTag?): Builder
        abstract fun project(project: Project): Builder
        abstract fun backing(project: Backing): Builder
        abstract fun user(project: User): Builder
        abstract fun build(): ProjectData
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_ProjectData.Builder()
        }
    }
}
