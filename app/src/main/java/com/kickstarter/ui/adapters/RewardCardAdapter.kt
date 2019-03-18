package com.kickstarter.ui.adapters

import android.view.View
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.RewardCardViewHolder

class RewardCardAdapter(private val delegate: Delegate) : KSAdapter() {

    interface Delegate : RewardCardViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int {
        return R.layout.item_reward_credit_card
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder = RewardCardViewHolder(view, delegate)

    fun takeCards(@NonNull cards: List<StoredCard>) {
        sections().clear()
        addSection(cards)
//        addSection(listOf(null))
        notifyDataSetChanged()
    }

}