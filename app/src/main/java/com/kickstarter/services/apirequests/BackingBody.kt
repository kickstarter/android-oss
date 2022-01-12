package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class BackingBody private constructor(
    private val backerCompletedAt: Int,
    private val backerNote: String?,
    private val id: Long,
    private val backer: Long,
) : Parcelable {
    fun backerCompletedAt() = this.backerCompletedAt
    fun backerNote() = this.backerNote
    fun id() = this.id
    fun backer() = this.backer

    @Parcelize
    data class Builder(
        private var backerCompletedAt: Int = 0,
        private var backerNote: String? = null,
        private var id: Long = 0L,
        private var backer: Long = 0L,
    ) : Parcelable {
        fun backerCompletedAt(backerCompletedAt: Int?) = apply { this.backerCompletedAt = backerCompletedAt ?: 0 }
        fun backerNote(backerNote: String?) = apply { this.backerNote = backerNote }
        fun id(id: Long?) = apply { this.id = id ?: 0 }
        fun backer(backer: Long?) = apply { this.backer = backer ?: 0L }
        fun build() = BackingBody(
            backerCompletedAt = backerCompletedAt,
            backerNote = backerNote,
            id = id,
            backer = backer
        )
    }

    fun toBuilder() = Builder(
        backerCompletedAt = backerCompletedAt,
        backerNote = backerNote,
        id = id,
        backer = backer
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is BackingBody) {
            equals = id() == other.id() &&
                backer() == other.backer() &&
                backerNote() == other.backerNote() &&
                backerCompletedAt() == other.backerCompletedAt()
        }
        return equals
    }
}
