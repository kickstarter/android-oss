package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Location private constructor(
    private val id: Long,
    private val city: String,
    private val country: String,
    private val displayableName: String,
    private val expandedCountry: String,
    private val name: String,
    private val projectsCount: Int,
    private val state: String
) : Parcelable, Relay {

    fun displayableName() = this.displayableName
    fun city() = this.city
    fun country() = this.country
    fun expandedCountry() = this.expandedCountry
    fun name() = this.name
    fun state() = this.state
    fun projectsCount() = this.projectsCount
    override fun id(): Long = this.id

    @Parcelize
    data class Builder(
        private var id: Long = 0L,
        private var city: String = "",
        private var country: String = "",
        private var displayableName: String = "",
        private var expandedCountry: String = "",
        private var name: String = "",
        private var projectsCount: Int = 0,
        private var state: String = ""
    ) : Parcelable {
        fun displayableName(dName: String?) = apply { dName?.let { this.displayableName = it } }
        fun city(city: String?) = apply { city?.let { this.city = it } }
        fun country(count: String?) = apply { count?.let { this.country = it } }
        fun expandedCountry(expCount: String?) = apply { expCount?.let { this.expandedCountry = it } }
        fun name(name: String?) = apply { name?.let { this.name = it } }
        fun state(state: String?) = apply { state?.let { this.state = it } }
        fun projectsCount(pCount: Int?) = apply { pCount?.let { this.projectsCount = it } }
        fun id(id: Long?) = apply { id?.let { this.id = it } }
        fun build() = Location(
            id = id,
            city = city,
            country = country,
            displayableName = displayableName,
            expandedCountry = expandedCountry,
            name = name,
            projectsCount = projectsCount,
            state = state
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        id = id,
        city = city,
        country = country,
        displayableName = displayableName,
        expandedCountry = expandedCountry,
        name = name,
        projectsCount = projectsCount,
        state = state
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Location) {
            equals = id() == other.id() &&
                city() == other.city() &&
                country() == other.country() &&
                displayableName() == other.displayableName() &&
                expandedCountry() == other.expandedCountry() &&
                name() == other.name() &&
                projectsCount() == other.projectsCount() &&
                state() == other.state()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
