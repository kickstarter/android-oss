package com.kickstarter.ui.adapters

import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RewardCardViewHolder
import com.kickstarter.ui.viewholders.RewardPledgeCardViewHolder

class RewardCardAdapter(private val delegate: Delegate) : KSAdapter() {
    interface Delegate : RewardCardViewHolder.Delegate, RewardPledgeCardViewHolder.Delegate

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun layout(sectionRow: SectionRow): Int {
        if (sectionRow.section() == 1) {
            return R.layout.item_reward_add_card
        } else {
            if (sectionRow.row() == selectedPosition) {
                return R.layout.item_reward_pledge_card
            }
            return R.layout.item_reward_credit_card
        }
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when (layout) {
            R.layout.item_reward_add_card -> EmptyViewHolder(view)
            R.layout.item_reward_pledge_card -> RewardPledgeCardViewHolder(view, this.delegate)
            else -> RewardCardViewHolder(view, this.delegate)
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
