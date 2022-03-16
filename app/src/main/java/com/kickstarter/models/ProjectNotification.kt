package com.kickstarter.models

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.libs.qualifiers.AutoGson
import kotlinx.parcelize.Parcelize

@Parcelize
class ProjectNotification private constructor(
    private val project: Project?,
    private val id: Long,
    private val email: Boolean,
    private val mobile: Boolean,
    private val urls: Urls?
) : Parcelable {
    fun project() = this.project
    fun id() = this.id
    fun email() = this.email
    fun mobile() = this.mobile
    fun urls() = this.urls

    @Parcelize
    data class Builder(
        private var project: Project? = null,
        private var id: Long = 0L,
        private var email: Boolean = false,
        private var mobile: Boolean = false,
        private var urls: Urls? = null
    ) : Parcelable {
        fun project(project: Project?) = apply { this.project = project }
        fun id(id: Long?) = apply { id?.let { this.id = it } }
        fun email(email: Boolean?) = apply { email?.let { this.email = it } }
        fun mobile(mobile: Boolean?) = apply { mobile?.let { this.mobile = it } }
        fun urls(urls: Urls?) = apply { this.urls = urls }
        fun build() = ProjectNotification(
            project = project,
            id = id,
            email = email,
            mobile = mobile,
            urls = urls
        )
    }

    fun toBuilder() = Builder(
        project = project,
        id = id,
        email = email,
        mobile = mobile,
        urls = urls
    )

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
            return Builder()
        }
    }
}
