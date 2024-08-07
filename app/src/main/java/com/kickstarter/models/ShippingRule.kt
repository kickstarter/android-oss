package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ShippingRule private constructor(
    private val id: Long?,
    private val cost: Double,
    private val location: Location?,
    private val estimatedMin: Double,
    private val estimatedMax: Double
) : Parcelable {
    fun id() = this.id
    fun cost() = this.cost
    fun location() = this.location
    fun estimatedMin() = this.estimatedMin
    fun estimatedMax() = this.estimatedMax

    @Parcelize
    data class Builder(
        private var id: Long? = -1L,
        private var cost: Double = 0.0,
        private var location: Location? = Location.builder().build(),
        private var estimatedMin: Double = 0.0,
        private var estimatedMax: Double = 0.0
    ) : Parcelable {
        fun id(id: Long?) = apply { this.id = id }
        fun cost(cost: Double) = apply { this.cost = cost }
        fun location(location: Location?) = apply { this.location = location }
        fun estimatedMin(estimatedMin: Double) = apply { this.estimatedMin = estimatedMin }
        fun estimatedMax(estimatedMax: Double) = apply { this.estimatedMax = estimatedMax }
        fun build() = ShippingRule(
            id = id,
            cost = cost,
            location = location,
            estimatedMin = estimatedMin,
            estimatedMax = estimatedMax
        )
    }

    override fun toString(): String {
        return location()?.displayableName() ?: ""
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is ShippingRule) {
            equals = id() == obj.id() &&
                cost() == obj.cost() &&
                location() == obj.location()
        }
        return equals
    }

    fun toBuilder() = Builder(
        id = id,
        cost = cost,
        location = location,
        estimatedMin = estimatedMin,
        estimatedMax = estimatedMax
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
