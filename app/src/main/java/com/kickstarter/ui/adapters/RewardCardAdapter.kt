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
    interface Delegate : RewardCardUnselectedViewHolder.Delegate, RewardAddCardViewHolder.Delegate

    private var selectedPosition = Pair(RecyclerView.NO_POSITION, CardState.SELECTED)

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
                        this.selectedPosition.second == CardState.SELECTED -> R.layout.item_reward_selected_card
                        else -> R.layout.item_reward_unselected_card
                    }
                }
                R.layout.item_reward_unselected_card
            } else {
                R.layout.item_add_card
            }
        }
    }

    override fun viewHolder(layout: Int, view: View): KSViewHolder {
        return when (layout) {
            R.layout.item_add_card -> RewardAddCardViewHolder(view, this.delegate)
            R.layout.item_reward_selected_card -> RewardCardSelectedViewHolder(view)
            R.layout.item_reward_unselected_card -> RewardCardUnselectedViewHolder(view, this.delegate)
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

    fun setSelectedPosition(position: Int) {
        this.selectedPosition = Pair(position, CardState.SELECTED)
        notifyDataSetChanged()
    }

    fun resetSelectedPosition() {
        this.selectedPosition = Pair(RecyclerView.NO_POSITION, CardState.SELECTED)
        notifyDataSetChanged()
    }

    fun insertCard(storedCardAndProject: Pair<StoredCard, Project>) : Int {
        val storedCards = sections()[0]
        val position = 0
        storedCards.add(position, storedCardAndProject)
        notifyItemInserted(position)

        return position
    }

}
