package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class SurveyResponse private constructor(
    private val answeredAt: DateTime?,
    private val id: Long,
    private val project: Project?,
    private val urls: Urls?
) : Parcelable {

    fun answeredAt() = this.answeredAt
    fun id() = this.id
    fun project() = this.project
    fun urls() = this.urls

    @Parcelize
    data class Builder(
        private var answeredAt: DateTime? = null,
        private var id: Long = 0L,
        private var project: Project? = null,
        private var urls: Urls? = null
    ) : Parcelable {

        fun answeredAt(answeredAt: DateTime?) = apply { answeredAt?.let { this.answeredAt = it } }
        fun id(id: Long) = apply { this.id = id ?: 0L }
        fun project(project: Project?) = apply { this.project = project }
        fun urls(urls: Urls?) = apply { urls?.let { this.urls = it } }
        fun build() = SurveyResponse(
            answeredAt = this.answeredAt,
            id = id,
            project = project,
            urls = this.urls
        )
    }

    @Parcelize
    class Urls private constructor(
        private val web: Web
    ) : Parcelable {
        fun web() = this.web

        @Parcelize
        data class Builder(
            private var web: Web = Web.builder().build()
        ) : Parcelable {
            fun web(web: Web?) = apply { this.web = web ?: Web.builder().build() }

            fun build() = Urls(
                web = web
            )
        }

        fun toBuilder() = Builder(
            web = web,
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(obj: Any?): Boolean {
            var equals = super.equals(obj)
            if (obj is Urls) {
                equals = web() == obj.web()
            }
            return equals
        }

        @Parcelize
        class Web private constructor(
            private val survey: String?
        ) : Parcelable {
            fun survey() = this.survey

            @Parcelize
            data class Builder(
                private var survey: String? = null
            ) : Parcelable {
                fun survey(survey: String?) = apply { this.survey = survey }
                fun build() = Web(
                    survey = survey
                )
            }

            fun toBuilder() = Builder(
                survey = survey
            )

            companion object {
                @JvmStatic
                fun builder() = Builder()
            }

            override fun equals(obj: Any?): Boolean {
                var equals = super.equals(obj)
                if (obj is Web) {
                    equals = survey() == obj.survey()
                }
                return equals
            }
        }
    }

    fun toBuilder() = Builder(
        answeredAt = answeredAt,
        id = id,
        project = project,
        urls = urls
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is SurveyResponse) {
            equals = answeredAt() == obj.answeredAt() &&
                id() == obj.id() &&
                project() == obj.project() &&
                urls() == obj.urls()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
