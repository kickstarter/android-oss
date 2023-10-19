package com.kickstarter.ui.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemLightsOnBinding
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.viewholders.EditorialViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class DiscoveryEditorialAdapter(private val delegate: Delegate) : KSListAdapter() {
    interface Delegate : EditorialViewHolder.Delegate

    fun setShouldShowEditorial(editorial: Editorial?) {
        clearSections()
        insertSection(SECTION_EDITORIAL_VIEW, emptyList<Editorial>())

        editorial?.let {
            setSection(SECTION_EDITORIAL_VIEW, listOf(editorial))
        } ?: setSection(SECTION_EDITORIAL_VIEW, emptyList<Editorial>())

        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.item_lights_on

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return EditorialViewHolder(
            ItemLightsOnBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ),
            delegate
        )
    }

    companion object {
        private const val SECTION_EDITORIAL_VIEW = 0
    }
}
