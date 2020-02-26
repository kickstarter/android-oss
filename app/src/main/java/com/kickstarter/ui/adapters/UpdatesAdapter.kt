package com.kickstarter.ui.adapters

import android.util.Pair
import android.view.View
import androidx.annotation.LayoutRes
import com.kickstarter.R
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
            R.layout.empty_view
        }
    }

    fun takeData(data: Pair<Project, List<Update>>) {
        val project = data.first
        val updates = data.second

        sections().clear()

        addSection(Observable.from(updates)
                .map { update -> Pair.create(project, update) }
                .toList().toBlocking().single())

        notifyDataSetChanged()
    }

    override fun viewHolder(@LayoutRes layout: Int, view: View): KSViewHolder {
        return if (layout == R.layout.item_update_card) {
            UpdateCardViewHolder(view, this.delegate)
        } else {
            EmptyViewHolder(view)
        }
    }
}
