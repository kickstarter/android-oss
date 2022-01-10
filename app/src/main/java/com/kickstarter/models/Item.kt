package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Item private constructor(
    private val amount: Float,
    private val description: String,
    private val id: Long,
    private val name: String,
    private val projectId: Long,
    private val taxable: Boolean
) : Parcelable {

    fun amount() = this.amount
    fun description() = this.description
    fun id() = this.id
    fun name() = this.name
    fun projectId() = this.projectId
    fun taxable() = this.taxable

    @Parcelize
    data class Builder(
        private var amount: Float = 0.0f,
        private var description: String = "",
        private var id: Long = 0,
        private var name: String = "",
        private var projectId: Long = 0,
        private var taxable: Boolean = false
    ) : Parcelable {
        fun amount(amount: Float?) = apply { this.amount = amount ?: 0.0f }
        fun description(description: String?) = apply { this.description = description ?: "" }
        fun id(id: Long?) = apply { this.id = id ?: 0 }
        fun name(name: String?) = apply { this.name = name ?: "" }
        fun projectId(projectId: Long?) = apply { this.projectId = projectId ?: 0 }
        fun taxable(taxable: Boolean?) = apply { this.taxable = taxable ?: false }
        fun build() = Item(
            amount = amount,
            description = description,
            id = id,
            name = name,
            projectId = projectId,
            taxable = taxable
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        amount = amount,
        description = description,
        id = id,
        name = name,
        projectId = projectId,
        taxable = taxable
    )

    override fun equals(other: Any?): Boolean =
        if (other is Item) {
            other.id() == this.id() &&
                other.amount() == this.amount() &&
                other.description() == this.description() &&
                other.name() == this.name() &&
                other.projectId() == this.projectId() &&
                other.taxable() == this.taxable()
        } else false

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
