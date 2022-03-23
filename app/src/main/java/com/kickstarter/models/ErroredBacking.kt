package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class ErroredBacking private constructor(
    private val project: Project
) : Parcelable {
    fun project() = this.project

    @Parcelize
    data class Builder(
        private var project: Project = Project.builder().build()
    ) : Parcelable {
        fun project(project: Project) = apply { this.project = project }
        fun build() = ErroredBacking(
            project = project
        )
    }

    fun toBuilder() = Builder(
        project = project
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is ErroredBacking) {
            equals = project() == other.project()
        }
        return equals
    }

    companion object {
        fun builder(): Builder = Builder()
    }

    @Parcelize
    class Project private constructor(
        private val finalCollectionDate: DateTime,
        private val name: String,
        private val slug: String,
    ) : Parcelable {

        fun finalCollectionDate() = this.finalCollectionDate
        fun name() = this.name
        fun slug() = this.slug

        @Parcelize
        data class Builder(
            private var finalCollectionDate: DateTime = DateTime.now(),
            private var name: String = "",
            private var slug: String = ""

        ) : Parcelable {
            fun finalCollectionDate(finalCollectionDate: DateTime?) = apply { this.finalCollectionDate = finalCollectionDate ?: DateTime.now() }
            fun name(name: String?) = apply { this.name = name ?: "" }
            fun slug(slug: String?) = apply { this.slug = slug ?: "" }
            fun build() = Project(
                finalCollectionDate = finalCollectionDate,
                name = name,
                slug = slug
            )
        }

        fun toBuilder() = Builder(
            finalCollectionDate = finalCollectionDate,
            name = name,
            slug = slug
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is Project) {
                equals = finalCollectionDate() == other.finalCollectionDate() &&
                    name() == other.name() &&
                    slug() == other.slug()
            }
            return equals
        }

        companion object {
            fun builder(): Builder = Builder()
        }
    }
}
