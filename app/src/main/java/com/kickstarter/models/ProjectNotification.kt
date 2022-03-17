package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ProjectNotification private constructor(
    private val project: Project,
    private val id: Long,
    private val email: Boolean,
    private val mobile: Boolean,
    private val urls: Urls
) : Parcelable {
    fun project() = this.project
    fun id() = this.id
    fun email() = this.email
    fun mobile() = this.mobile
    fun urls() = this.urls

    @Parcelize
    data class Builder(
        private var project: Project = Project.builder().build(),
        private var id: Long = 0L,
        private var email: Boolean = false,
        private var mobile: Boolean = false,
        private var urls: Urls = Urls.builder().build()
    ) : Parcelable {
        fun project(project: Project?) = apply { this.project = project ?: Project.builder().build() }
        fun id(id: Long?) = apply { id?.let { this.id = it } }
        fun email(email: Boolean?) = apply { email?.let { this.email = it } }
        fun mobile(mobile: Boolean?) = apply { mobile?.let { this.mobile = it } }
        fun urls(urls: Urls?) = apply { this.urls = urls ?: Urls.builder().build() }
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

    @Parcelize
    class Project private constructor(
        private val name: String,
        private val id: Long
    ) : Parcelable {
        fun name() = this.name
        fun id() = this.id

        @Parcelize
        data class Builder(
            private var name: String = "",
            private var id: Long = 0L
        ) : Parcelable {
            fun name(name: String?) = apply { this.name = name ?: "" }
            fun id(id: Long?) = apply { this.id = id ?: 0L }
            fun build() = Project(
                name = name,
                id = id
            )
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    @Parcelize
    class Urls private constructor(
        private val api: Api
    ) : Parcelable {
        fun api() = this.api

        @Parcelize
        data class Builder(
            private var api: Api = Api.builder().build()
        ) : Parcelable {
            fun api(api: Api?) = apply { this.api = api ?: Api.builder().build() }
            fun build() = Urls(api = api)
        }

        @Parcelize
        class Api private constructor(
            private val notification: String
        ) : Parcelable {
            fun notification() = this.notification

            @Parcelize
            data class Builder(
                private var notification: String = ""
            ) : Parcelable {
                fun notification(notification: String?) = apply { this.notification = notification ?: "" }
                fun build() = Api(notification = notification)
            }

            companion object {
                @JvmStatic
                fun builder(): Builder {
                    return Builder()
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

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
