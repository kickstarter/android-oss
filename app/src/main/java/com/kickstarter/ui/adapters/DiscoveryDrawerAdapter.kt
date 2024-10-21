package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryDrawerChildFilterViewBinding
import com.kickstarter.databinding.DiscoveryDrawerHeaderBinding
import com.kickstarter.databinding.DiscoveryDrawerLoggedInViewBinding
import com.kickstarter.databinding.DiscoveryDrawerLoggedOutViewBinding
import com.kickstarter.databinding.DiscoveryDrawerParentFilterViewBinding
import com.kickstarter.databinding.DiscoveryDrawerTopFilterViewBinding
import com.kickstarter.databinding.EmptyViewBinding
import com.kickstarter.models.User
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.EmptyViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.HeaderViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder
import java.util.ArrayList

class DiscoveryDrawerAdapter(
    private val delegate: Delegate,
) : KSAdapter() {
    private var drawerData: NavigationDrawerData? = null

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    interface Delegate :
        LoggedInViewHolder.Delegate,
        LoggedOutViewHolder.Delegate,
        TopFilterViewHolder.Delegate,
        ParentFilterViewHolder.Delegate,
        ChildFilterViewHolder.Delegate

    override fun layout(sectionRow: SectionRow): Int {
        val obj = objectFromSectionRow(sectionRow)
        return when (sectionRow.section()) {
            0 -> if (obj == null) R.layout.discovery_drawer_logged_out_view else R.layout.discovery_drawer_logged_in_view
            else -> obj?.let { layoutForDatum(it, sectionRow) } ?: 0
        }
    }

    private fun layoutForDatum(datum: Any, sectionRow: SectionRow): Int {
        if (datum is NavigationDrawerData.Section.Row) {
            return if (sectionRow.row() == 0) {
                if (datum.params().isCategorySet) R.layout.discovery_drawer_parent_filter_view else R.layout.discovery_drawer_top_filter_view
            } else {
                R.layout.discovery_drawer_child_filter_view
            }
        } else if (datum is Int) {
            return R.layout.discovery_drawer_header
        }
        return R.layout.horizontal_line_1dp_view
    }

    override fun objectFromSectionRow(sectionRow: SectionRow): Any? {
        val obj = super.objectFromSectionRow(sectionRow) ?: return null
        if (obj is User || obj is Int) {
            return obj
        }

        val row = obj as NavigationDrawerData.Section.Row
        val expanded = if (row.params().category() == null || drawerData?.expandedCategory() == null) {
            false
        } else {
            row.params().category()?.rootId() == drawerData?.expandedCategory()?.rootId()
        }
        return row
            .toBuilder()
            .selected(row.params() == drawerData?.selectedParams())
            .rootIsExpanded(expanded)
            .build()
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.discovery_drawer_logged_in_view -> LoggedInViewHolder(
                DiscoveryDrawerLoggedInViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate,
            )
            R.layout.discovery_drawer_logged_out_view -> LoggedOutViewHolder(
                DiscoveryDrawerLoggedOutViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.discovery_drawer_parent_filter_view -> ParentFilterViewHolder(
                DiscoveryDrawerParentFilterViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.discovery_drawer_top_filter_view -> TopFilterViewHolder(
                DiscoveryDrawerTopFilterViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.discovery_drawer_child_filter_view -> ChildFilterViewHolder(
                DiscoveryDrawerChildFilterViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                ),
                delegate
            )
            R.layout.discovery_drawer_header -> HeaderViewHolder(
                DiscoveryDrawerHeaderBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false
                )
            )
            R.layout.horizontal_line_1dp_view -> EmptyViewHolder(
                EmptyViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )
            else -> EmptyViewHolder(
                EmptyViewBinding.inflate(
                    LayoutInflater.from(
                        viewGroup.context
                    ),
                    viewGroup, false
                )
            )
        }
    }

    fun takeData(data: NavigationDrawerData) {
        drawerData = data
        sections().clear()
        sections().addAll(sectionsFromData(data))
        notifyDataSetChanged()
    }

    private fun sectionsFromData(data: NavigationDrawerData): List<List<Any?>> {
        val newSections: MutableList<List<Any?>> = ArrayList()
        newSections.add(listOf<Any?>(data.user()))
        newSections.add(listOf<Any?>(null)) // Divider
        newSections.add(listOf<Any>(R.string.Collections))

        val top = data.sections().filter { it.isTopFilter }
        val category = data.sections().filter { it.isCategoryFilter }

        for (section in top) {
            newSections.add(ArrayList<Any?>(section.rows()))
        }

        newSections.add(listOf<Any?>(null)) // Divider
        newSections.add(listOf<Any>(R.string.discovery_filters_categories_title))

        for (section in category) {
            newSections.add(ArrayList<Any?>(section.rows()))
        }
        return newSections
    }

    init {
        setHasStableIds(true)
    }
}
