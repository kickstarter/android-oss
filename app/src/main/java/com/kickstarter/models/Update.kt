package com.kickstarter.models

import android.os.Parcelable
import android.text.Html
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class Update private constructor(
    private val body: String?,
    private val commentsCount: Int?,
    private val hasLiked: Boolean?,
    private val id: Long,
    private val isPublic: Boolean?,
    private val likesCount: Int?,
    private val projectId: Long,
    private val publishedAt: DateTime?,
    private val sequence: Int,
    private val title: String,
    private val updatedAt: DateTime?,
    private val urls: Urls?,
    private val user: User?,
    private val visible: Boolean?
) : Parcelable {
    fun body() = this.body
    fun commentsCount() = this.commentsCount
    fun hasLiked() = this.hasLiked
    fun id() = this.id
    fun isPublic() = this.isPublic
    fun likesCount() = this.likesCount
    fun projectId() = this.projectId
    fun publishedAt() = this.publishedAt
    fun sequence() = this.sequence
    fun title() = this.title
    fun updatedAt() = this.updatedAt
    fun urls() = this.urls
    fun user() = this.user
    fun visible() = this.visible

    @Parcelize
    data class Builder(
        private var body: String? = null,
        private var commentsCount: Int? = null,
        private var hasLiked: Boolean? = null,
        private var id: Long = 0L,
        private var isPublic: Boolean? = null,
        private var likesCount: Int? = null,
        private var projectId: Long = 0L,
        private var publishedAt: DateTime? = null,
        private var sequence: Int = 0,
        private var title: String = "",
        private var updatedAt: DateTime? = null,
        private var urls: Urls? = null,
        private var user: User? = null,
        private var visible: Boolean? = null
    ) : Parcelable {
        fun body(body: String?) = apply { this.body = body }
        fun commentsCount(commentsCount: Int?) = apply { this.commentsCount = commentsCount }
        fun hasLiked(hasLiked: Boolean?) = apply { this.hasLiked = hasLiked }
        fun id(id: Long) = apply { this.id = id }
        fun isPublic(isPublic: Boolean?) = apply { this.isPublic = isPublic }
        fun likesCount(likesCount: Int?) = apply { this.likesCount = likesCount }
        fun projectId(projectId: Long) = apply { this.projectId = projectId }
        fun publishedAt(publishedAt: DateTime?) = apply { this.publishedAt = publishedAt }
        fun sequence(sequence: Int) = apply { this.sequence = sequence }
        fun title(title: String) = apply { this.title = title }
        fun updatedAt(updatedAt: DateTime?) = apply { this.updatedAt = updatedAt }
        fun urls(urls: Urls?) = apply { this.urls = urls }
        fun user(user: User?) = apply { this.user = user }
        fun visible(visible: Boolean?) = apply { this.visible = visible }
        fun build() = Update(
            body = body,
            commentsCount = commentsCount,
            hasLiked = hasLiked,
            id = id,
            isPublic = isPublic,
            likesCount = likesCount,
            projectId = projectId,
            publishedAt = publishedAt,
            sequence = sequence,
            title = title,
            updatedAt = updatedAt,
            urls = urls,
            user = user,
            visible = visible
        )
    }

    fun toBuilder() = Builder(
        body = body,
        commentsCount = commentsCount,
        hasLiked = hasLiked,
        id = id,
        isPublic = isPublic,
        likesCount = likesCount,
        projectId = projectId,
        publishedAt = publishedAt,
        sequence = sequence,
        title = title,
        updatedAt = updatedAt,
        urls = urls,
        user = user,
        visible = visible
    )

    @Parcelize
    class Urls private constructor(
        private val web: Web
    ) : Parcelable {
        fun web() = this.web

        @Parcelize
        data class Builder(
            private var web: Web = Web.builder().build(),
        ) : Parcelable {
            fun web(web: Web?) = apply { this.web = web ?: Web.builder().build() }
            fun build() = Urls(
                web = web
            )
        }

        fun toBuilder() = Builder(
            web = web
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
            private val likes: String?,
            private val update: String
        ) : Parcelable {
            fun likes() = this.likes
            fun update() = this.update

            @Parcelize
            data class Builder(
                private var update: String = "",
                private var likes: String? = null
            ) : Parcelable {
                fun likes(likes: String?) = apply { this.likes = likes }
                fun update(update: String?) = apply { this.update = update ?: "" }
                fun build() = Web(
                    likes = likes,
                    update = update
                )
            }

            fun toBuilder() = Builder(
                likes = likes,
                update = update
            )

            companion object {
                @JvmStatic
                fun builder() = Builder()
            }

            override fun equals(obj: Any?): Boolean {
                var equals = super.equals(obj)
                if (obj is Web) {
                    equals = likes() == obj.likes() &&
                        update() == obj.update()
                }
                return equals
            }
        }
    }

    fun truncatedBody(): String {
        try {
            var str = Html.fromHtml(body()).toString()
            if (str.length > TRUNCATED_BODY_LENGTH) {
                str = str.substring(0, TRUNCATED_BODY_LENGTH - 1) + "\u2026"
            }
            return str
        } catch (ignore: NullPointerException) {
        }
        return ""
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
        private const val TRUNCATED_BODY_LENGTH = 400
    }
    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Update) {
            equals = body() == obj.body() &&
                commentsCount() == commentsCount() &&
                hasLiked() == hasLiked() &&
                id() == id() &&
                isPublic() == isPublic() &&
                likesCount() == likesCount() &&
                projectId() == projectId() &&
                publishedAt() == publishedAt() &&
                sequence() == sequence() &&
                title() == title() &&
                updatedAt() == updatedAt() &&
                urls() == urls() &&
                user() == user() &&
                visible() == visible()
        }
        return equals
    }
}
