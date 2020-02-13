package com.kickstarter.ui.data

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project

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

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun refTagFromIntent(intentRefTag: RefTag?): Builder
        abstract fun refTagFromCookie(cookieRefTag: RefTag?): Builder
        abstract fun project(project: Project): Builder
        abstract fun build(): ProjectData
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_ProjectData.Builder()
        }
    }
}
