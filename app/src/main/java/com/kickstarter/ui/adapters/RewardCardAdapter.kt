package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.viewholders.*
import rx.Observable

class RewardCardAdapter(private val delegate: Delegate) : KSAdapter() {
    interface Delegate : RewardCardViewHolder.Delegate, RewardPledgeCardViewHolder.Delegate, RewardAddCardViewHolder.Delegate

    private var enabled = true
    private var selectedPosition = Pair(RecyclerView.NO_POSITION, CardState.SELECT)

    init {
        val placeholders = arrayOfNulls<Any>(3).toList()
        addSection(placeholders)
    }

    override fun layout(sectionRow: SectionRow): Int {
        return if (sections().size == 1) {
            R.layout.item_reward_placeholder_card
        } else {
            if (sectionRow.section() == 0) {
                if (sectionRow.row() == this.selectedPosition.first) {
                    return when {
                        this.selectedPosition.second == CardState.SELECT -> R.layout.item_reward_credit_card
                        this.selectedPosition.second == CardState.PLEDGE -> R.layout.item_reward_pledge_card
                        else -> R.layout.item_reward_loading_card
                    }
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
            R.layout.item_reward_pledge_card -> RewardPledgeCardViewHolder(view, this.delegate, this.enabled)
            R.layout.item_reward_credit_card -> RewardCardViewHolder(view, this.delegate)
            R.layout.item_reward_loading_card -> RewardLoadingCardViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    fun takeCards(cards: List<StoredCard>, project: Project) {
        sections().clear()
        addSection(Observable.from(cards)
                .map { Pair(it, project) }
                .toList().toBlocking().single()
        )
        addSection(listOf(null))
        notifyDataSetChanged()
    }

    fun setLoadingPosition(position: Int) {
        this.selectedPosition = Pair(position, CardState.LOADING)
        notifyItemChanged(position)
    }

    fun setPledgePosition(position: Int) {
        this.selectedPosition = Pair(position, CardState.PLEDGE)
        notifyItemChanged(position)
    }

    fun resetPledgePosition(position: Int) {
        this.selectedPosition = Pair(RecyclerView.NO_POSITION, CardState.SELECT)
        notifyItemChanged(position)
    }

    fun insertCard(storedCardAndProject: Pair<StoredCard, Project>) : Int {
        val storedCards = sections()[0]
        val position = 0
        storedCards.add(position, storedCardAndProject)
        notifyItemInserted(position)

        return position
    }

    fun setPledgeEnabled(enabled: Boolean) {
        val selectedIndex = this.selectedPosition.first
        if (selectedIndex != RecyclerView.NO_POSITION) {
            this.enabled = enabled
            notifyItemChanged(this.selectedPosition.first)
        }
    }

}
