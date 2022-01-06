package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class RewardsItem private constructor(
    private val id: Long,
    private val item: Item, // TODO
    private val itemId: Long,
    private val quantity: Int,
    private val rewardId: Long?,
    private val hasBackers: Boolean?
) : Parcelable {

    fun id() = this.id
    fun item() = this.item
    fun itemId() = this.itemId
    fun quantity() = this.quantity
    fun rewardId() = this.rewardId
    fun hasBackers() = this.hasBackers

    @Parcelize
    data class Builder(
        private var id: Long = 0,
        private var item: Item = Item.builder().build(),
        private var itemId: Long = 0,
        private var quantity: Int = 0,
        private var rewardId: Long? = 0,
        private var hasBackers: Boolean? = false
    ) : Parcelable {
        fun id(id: Long?) = apply { this.id = id ?: 0 }
        fun item(item: Item?) = apply { this.item = item ?: Item.builder().build() }
        fun itemId(itemId: Long?) = apply { this.itemId = itemId ?: 0 }
        fun quantity(quantity: Int?) = apply { this.quantity = quantity ?: 0 }
        fun rewardId(rewardId: Long?) = apply { this.rewardId = rewardId ?: 0 }
        fun hasBackers(hasBackers: Boolean?) = apply { this.hasBackers = hasBackers }
        fun build() = RewardsItem(id = id, item = item, itemId = itemId, quantity = quantity, rewardId = rewardId, hasBackers = hasBackers)
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(id = id, item = item, itemId = itemId, quantity = quantity, rewardId = rewardId, hasBackers = hasBackers)

    override fun equals(other: Any?): Boolean =
        if (other is RewardsItem) {
            other.id() == this.id() && other.item() == this.item() &&
                other.itemId() == this.itemId() &&
                other.quantity() == this.quantity() &&
                other.rewardId() == this.rewardId() &&
                other.hasBackers() == this.hasBackers()
        } else false

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
