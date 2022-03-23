package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class CreatorDetails(
    private val backingsCount: Int,
    private val launchedProjectsCount: Int,
) : Parcelable {
    fun backingsCount() = this.backingsCount
    fun launchedProjectsCount() = this.launchedProjectsCount

    @Parcelize
    data class Builder(
        private var backingsCount: Int = 0,
        private var launchedProjectsCount: Int = 0
    ) : Parcelable {
        fun backingsCount(backingsCount: Int) = apply { this.backingsCount = backingsCount }
        fun launchedProjectsCount(launchedProjectsCount: Int) = apply {
            this.launchedProjectsCount = launchedProjectsCount
        }
        fun build() = CreatorDetails(
            backingsCount = backingsCount,
            launchedProjectsCount = launchedProjectsCount
        )
    }

    fun toBuilder() = Builder(
        backingsCount = backingsCount,
        launchedProjectsCount = launchedProjectsCount
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is CreatorDetails) {
            equals = backingsCount() == obj.backingsCount() &&
                launchedProjectsCount() == obj.launchedProjectsCount()
        }
        return equals
    }

    companion object {
        fun builder() = Builder()
    }
}
