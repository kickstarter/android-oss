package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyUpdatesLayoutBinding
import com.kickstarter.databinding.ItemUpdateCardBinding
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.UpdateCardViewHolder
import rx.Observable

class UpdatesAdapter(private val delegate: Delegate) : KSAdapter() {

    interface Delegate : UpdateCardViewHolder.Delegate

    @LayoutRes
    override fun layout(sectionRow: SectionRow): Int {
        return if (sectionRow.section() == 0) {
            R.layout.item_update_card
        } else {
            R.layout.empty_updates_layout
        }
    }

    fun takeData(data: Pair<Project, List<Update>>) {
        val project = data.first
        val updates = data.second

        sections().clear()

        addSection(
            Observable.from(updates)
                .map { update -> Pair.create(project, update) }
                .toList().toBlocking().single()
        )

        if (updates.isEmpty()) {
            sections().add(listOf(Pair<Project, List<Update>>(project, emptyList())))
        }

        notifyDataSetChanged()
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return if (layout == R.layout.item_update_card) {
            UpdateCardViewHolder(ItemUpdateCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false), this.delegate)
        } else {
            EmptyViewHolder(EmptyUpdatesLayoutBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }
    }
}
