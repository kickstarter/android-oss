package com.kickstarter.models

import auto.parcel.AutoParcel
import com.kickstarter.libs.qualifiers.AutoGson
import android.os.Parcelable

@AutoParcel
@AutoGson
abstract class ProjectNotification : Parcelable {
    abstract fun project(): Project?
    abstract fun id(): Long
    abstract fun email(): Boolean
    abstract fun mobile(): Boolean
    abstract fun urls(): Urls?

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun project(project: Project?): Builder
        abstract fun id(id: Long): Builder
        abstract fun email(email: Boolean): Builder
        abstract fun mobile(mobile: Boolean): Builder
        abstract fun urls(urls: Urls?): Builder
        abstract fun build(): ProjectNotification?
    }

    abstract fun toBuilder(): Builder

    @AutoParcel
    @AutoGson
    abstract class Project : Parcelable {
        abstract fun name(): String?
        abstract fun id(): Long

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun name(name: String?): Builder?
            abstract fun id(id: Long): Builder?
            abstract fun build(): Project?
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return AutoParcel_ProjectNotification_Project.Builder()
            }
        }
    }

    @AutoParcel
    @AutoGson
    abstract class Urls : Parcelable {
        abstract fun api(): Api?

        @AutoParcel.Builder
        abstract class Builder {
            abstract fun api(api: Api?): Builder?
            abstract fun build(): Urls?
        }

        @AutoParcel
        @AutoGson
        abstract class Api : Parcelable {
            abstract fun notification(): String?

            @AutoParcel.Builder
            abstract class Builder {
                abstract fun notification(notification: String?): Builder
                abstract fun build(): Api?
            }

            companion object {
                @JvmStatic
                fun builder(): Builder {
                    return AutoParcel_ProjectNotification_Urls_Api.Builder()
                }
            }
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return AutoParcel_ProjectNotification_Urls.Builder()
            }
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return AutoParcel_ProjectNotification.Builder()
        }
    }
}