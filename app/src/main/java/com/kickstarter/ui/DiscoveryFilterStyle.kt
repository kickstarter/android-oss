package com.kickstarter.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class DiscoveryFilterStyle private constructor(
    private val light: Boolean,
    private val primary: Boolean,
    private val selected: Boolean,
    private val showLiveProjectsCount: Boolean,
    private val visible: Boolean
) : Parcelable {
    fun light() = this.light
    fun primary() = this.primary
    fun selected() = this.selected
    fun showLiveProjectsCount() = this.showLiveProjectsCount
    fun visible() = this.visible

    @Parcelize
    data class Builder(
        private var light: Boolean = false,
        private var primary: Boolean = false,
        private var selected: Boolean = false,
        private var showLiveProjectsCount: Boolean = false,
        private var visible: Boolean = false,
    ) : Parcelable {
        fun light(light: Boolean?) = apply { this.light = light ?: false }
        fun primary(primary: Boolean?) = apply { this.primary = primary ?: false }
        fun selected(selected: Boolean?) = apply { this.selected = selected ?: false }
        fun showLiveProjectsCount(showLiveProjectsCount: Boolean?) =
            apply { this.showLiveProjectsCount = showLiveProjectsCount ?: false }

        fun visible(visible: Boolean?) = apply { this.visible = visible ?: false }
        fun build() = DiscoveryFilterStyle(
            light = light,
            primary = primary,
            showLiveProjectsCount = showLiveProjectsCount,
            selected = selected,
            visible = visible
        )
    }

    fun toBuilder() = Builder(
        light = light,
        primary = primary,
        showLiveProjectsCount = showLiveProjectsCount,
        selected = selected,
        visible = visible
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is DiscoveryFilterStyle) {
            equals = light() == obj.light() &&
                primary() == obj.primary() &&
                selected() == obj.selected() &&
                visible() == obj.visible()
        }
        return equals
    }

    companion object {
        fun builder() = Builder().showLiveProjectsCount(false)
    }
}
