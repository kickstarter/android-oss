package com.kickstarter.ui.adapters

import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.viewholders.*

class RewardCardAdapter(private val delegate: Delegate) : KSAdapter() {
    interface Delegate : RewardCardViewHolder.Delegate, RewardPledgeCardViewHolder.Delegate, RewardAddCardViewHolder.Delegate

    private var selectedPosition = RecyclerView.NO_POSITION

    init {
        val placeholders = arrayOfNulls<Any>(3).toList()
        addSection(placeholders)
    }

    override fun layout(sectionRow: SectionRow): Int {
        return if (sections().size == 1) {
            R.layout.item_reward_placeholder_card
        } else {
            if (sectionRow.section() == 0) {
                if (sectionRow.row() == this.selectedPosition) {
                    return R.layout.item_reward_pledge_card
                }
                R.layout.item_reward_credit_card
            } else {
                R.layout.item_reward_add_card
            }
        }
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when (layout) {
            R.layout.item_reward_add_card -> RewardAddCardViewHolder(view, this.delegate)
            R.layout.item_reward_pledge_card -> RewardPledgeCardViewHolder(view, this.delegate)
            R.layout.item_reward_credit_card -> RewardCardViewHolder(view, this.delegate)
            else -> EmptyViewHolder(view)
        }
    }

    fun takeCards(@NonNull cards: List<StoredCard>) {
        sections().clear()
        addSection(cards)
        addSection(listOf(null))
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        this.selectedPosition = position
        notifyItemChanged(position)
    }

    fun resetSelectedPosition(position: Int) {
        this.selectedPosition = RecyclerView.NO_POSITION
        notifyItemChanged(position)
    }

}
