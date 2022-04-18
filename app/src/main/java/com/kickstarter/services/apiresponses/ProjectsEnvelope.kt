package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.Project
import kotlinx.parcelize.Parcelize

@Parcelize
class ProjectsEnvelope private constructor(
    private val projects: List<Project>,
    private val urls: UrlsEnvelope
) : Parcelable {
    fun projects() = this.projects
    fun urls() = this.urls

    @Parcelize
    data class Builder(
        private var projects: List<Project> = emptyList(),
        private var urls: UrlsEnvelope = UrlsEnvelope.builder().build()
    ) : Parcelable {
        fun projects(projects: List<Project>) = apply { this.projects = projects }
        fun urls(urls: UrlsEnvelope) = apply { this.urls = urls }
        fun build() = ProjectsEnvelope(
            projects = projects,
            urls = urls
        )
    }

    fun toBuilder() = Builder(
        projects = projects,
        urls = urls
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is ProjectsEnvelope) {
            equals = projects() == other.projects() &&
                urls() == other.urls()
        }
        return equals
    }

    @Parcelize
    class UrlsEnvelope private constructor(
        private val api: ApiEnvelope
    ) : Parcelable {
        fun api() = this.api

        @Parcelize
        data class Builder(
            private var api: ApiEnvelope = ApiEnvelope.builder().build()
        ) : Parcelable {
            fun api(api: ApiEnvelope) = apply { this.api = api }
            fun build() = UrlsEnvelope(
                api = api
            )
        }

        fun toBuilder() = Builder(
            api = api
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is UrlsEnvelope) {
                equals = api() == other.api()
            }
            return equals
        }

        @Parcelize
        class ApiEnvelope private constructor(
            private val moreProjects: String
        ) : Parcelable {
            fun moreProjects() = this.moreProjects

            @Parcelize
            data class Builder(
                private var moreProjects: String = ""
            ) : Parcelable {
                fun moreProjects(moreProjects: String?) = apply { this.moreProjects = moreProjects ?: "" }
                fun build() = ApiEnvelope(
                    moreProjects = moreProjects
                )
            }

            fun toBuilder() = Builder(
                moreProjects = moreProjects
            )

            companion object {
                @JvmStatic
                fun builder() = Builder()
            }

            override fun equals(other: Any?): Boolean {
                var equals = super.equals(other)
                if (other is ApiEnvelope) {
                    equals = moreProjects() == other.moreProjects()
                }
                return equals
            }
        }
    }
}
