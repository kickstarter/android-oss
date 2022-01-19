package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.ApolloEnvelope
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.parcelize.Parcelize

@Parcelize
class DiscoverEnvelope private constructor(
    private val projects: List<Project>,
    /**
     urls: contains the pagination url information for next projects page on V1, not necessary on GraphQL
     */
    private val urls: UrlsEnvelope?,
    /**
     stats: information only necessary for the creator
     */
    private val stats: StatsEnvelope?,
    /**
     pageInfoEnvelope: contains the cursor for pagination with GraphQL, not necessary with V1
     */
    private val pageInfoEnvelope: PageInfoEnvelope?
) : Parcelable, ApolloEnvelope {
    fun projects() = this.projects
    fun urls() = this.urls
    fun stats() = this.stats
    override fun pageInfoEnvelope() = this.pageInfoEnvelope

    @Parcelize
    data class Builder(
        private var projects: List<Project> = emptyList(),
        private var urls: UrlsEnvelope? = null,
        private var stats: StatsEnvelope? = null,
        private var pageInfoEnvelope: PageInfoEnvelope? = null
    ) : Parcelable {
        fun projects(projects: List<Project>?) = apply { this.projects = projects ?: emptyList() }
        fun urls(urls: UrlsEnvelope?) = apply { this.urls = urls }
        fun stats(stats: StatsEnvelope?) = apply { this.stats = stats }
        fun pageInfoEnvelope(pageInfoEnvelope: PageInfoEnvelope?) = apply { this.pageInfoEnvelope = pageInfoEnvelope }
        fun build() = DiscoverEnvelope(
            projects = projects,
            urls = urls,
            stats = stats,
            pageInfoEnvelope = pageInfoEnvelope
        )
    }

    fun toBuilder() = Builder(
        projects = projects,
        urls = urls,
        stats = stats
    )

    @Parcelize
    class UrlsEnvelope private constructor(
        private val api: ApiEnvelope?
    ) : Parcelable {
        fun api() = this.api

        @Parcelize
        data class Builder(
            private var api: ApiEnvelope? = null
        ) : Parcelable {
            fun api(api: ApiEnvelope?) = apply { this.api = api }
            fun build() = UrlsEnvelope(api = api)
        }

        fun toBuilder() = Builder(api = api)

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
                fun build() = ApiEnvelope(moreProjects = moreProjects)
            }

            fun toBuilder() = Builder(moreProjects = moreProjects)

            companion object {
                @JvmStatic
                fun builder() = Builder()
            }
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    @Parcelize
    class StatsEnvelope private constructor(
        private val count: Int
    ) : Parcelable {
        fun count() = this.count

        @Parcelize
        data class Builder(
            private var count: Int = 0
        ) : Parcelable {
            fun count(count: Int?) = apply { this.count = count ?: 0 }
            fun build() = StatsEnvelope(count = count)
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
