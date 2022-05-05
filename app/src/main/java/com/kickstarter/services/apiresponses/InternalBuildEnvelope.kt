package com.kickstarter.services.apiresponses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class InternalBuildEnvelope private constructor(
    private val build: Int?,
    private val changelog: String?,
    private val newerBuildAvailable: Boolean
) : Parcelable {
    fun build() = this.build
    fun changelog() = this.changelog
    fun newerBuildAvailable() = this.newerBuildAvailable

    @Parcelize
    data class Builder(
        private var build: Int? = null,
        private var changelog: String? = null,
        private var newerBuildAvailable: Boolean = false
    ) : Parcelable {
        fun build(build: Int?) = apply { this.build = build ?: 0 }
        fun changelog(changelog: String?) = apply { this.changelog = changelog ?: "" }
        fun newerBuildAvailable(newerBuildAvailable: Boolean?) = apply { this.newerBuildAvailable = newerBuildAvailable ?: false }
        fun build() = InternalBuildEnvelope(
            build = build,
            changelog = changelog,
            newerBuildAvailable = newerBuildAvailable
        )
    }

    fun toBuilder() = Builder(
        build = build,
        changelog = changelog,
        newerBuildAvailable = newerBuildAvailable
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is InternalBuildEnvelope) {
            equals = build() == other.build() &&
                changelog() == other.changelog() &&
                newerBuildAvailable() == other.newerBuildAvailable()
        }
        return equals
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
