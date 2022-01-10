package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SingleLocation private constructor(
    private val id: Long,
    private val localizedName: String
) : Parcelable {
    fun id() = this.id
    fun localizedName() = this.localizedName

    @Parcelize
    data class Builder(
        private var id: Long = 0L,
        private var localizedName: String = ""
    ) : Parcelable {
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun localizedName(localizedName: String?) = apply { this.localizedName = localizedName ?: "" }
        fun build() = SingleLocation(
            id = id,
            localizedName = localizedName
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is SingleLocation) {
            equals = id() == obj.id() &&
                localizedName() == obj.localizedName()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun toBuilder() = Builder(
        id = id,
        localizedName = localizedName
    )

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
