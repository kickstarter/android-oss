package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * PledgeManager Data Model
 *
 */
@Parcelize
class PledgeManager private constructor(
    private val id: Long?,
    private val acceptsNewBackers: Boolean, // Whether the pledge manager accepts new backers or not
    private val optedOut: Boolean?, // Whether the creator opted out of the pledge manager.
    private val state: PledgeManagerStateEnum? // The pledge manager's state, e.g. draft, submitted, approved, denied, etc.
) : Parcelable {

    fun id() = this.id
    fun acceptsNewBackers() = this.acceptsNewBackers
    fun optedOut() = this.optedOut
    fun state() = this.state

    @Parcelize
    data class Builder(
        private var id: Long? = -1,
        private var acceptsNewBackers: Boolean = false,
        private var optedOut: Boolean? = false,
        private var state: PledgeManagerStateEnum? = PledgeManagerStateEnum.DRAFT
    ) : Parcelable {
        fun id(id: Long?) = apply { this.id = id }

        fun acceptsNewBackers(acceptsNewBackers: Boolean) = apply { this.acceptsNewBackers = acceptsNewBackers }
        fun optedOut(optedOut: Boolean?) = apply { this.optedOut = optedOut }
        fun state(state: PledgeManagerStateEnum?) = apply { this.state = state }
        fun build() = PledgeManager(id = id, acceptsNewBackers = acceptsNewBackers, optedOut = optedOut, state = state)
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(id = id, acceptsNewBackers = acceptsNewBackers, optedOut = optedOut, state = state)

    override fun equals(other: Any?): Boolean =
        if (other is PledgeManager) {
            other.id == id && other.acceptsNewBackers == acceptsNewBackers && other.optedOut == optedOut && other.state == state
        } else false

    enum class PledgeManagerStateEnum(private val type: String) {
        DRAFT("draft"),
        SUBMITTED("submitted"),
        APPROVED("approved"),
        DENIED("denied"),
    }
}
