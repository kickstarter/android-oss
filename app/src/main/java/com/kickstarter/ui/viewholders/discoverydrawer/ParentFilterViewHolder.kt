package com.kickstarter.ui.viewholders.discoverydrawer

import android.view.View
import com.kickstarter.databinding.DiscoveryDrawerParentFilterViewBinding
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.KSViewHolder

class ParentFilterViewHolder(
    private val binding: DiscoveryDrawerParentFilterViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private lateinit var item: NavigationDrawerData.Section.Row

    interface Delegate {
        fun parentFilterViewHolderRowClick(viewHolder: ParentFilterViewHolder, row: NavigationDrawerData.Section.Row)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        item = requireNotNull(data as NavigationDrawerData.Section.Row) { NavigationDrawerData.Section.Row::class.java.toString() + " required to be non-null." }
    }

    override fun onBind() {
        val context = context()
        val ksString = requireNotNull(environment().ksString())
        binding.filterTextView.text = item.params().filterString(context, ksString, false, true)
        if (item.rootIsExpanded()) {
            binding.expandButton.visibility = View.GONE
            binding.collapseButton.visibility = View.VISIBLE
        } else {
            binding.expandButton.visibility = View.VISIBLE
            binding.collapseButton.visibility = View.GONE
        }
        binding.filterView.setOnClickListener {
            rowClick()
        }
    }

    private fun rowClick() {
        delegate.parentFilterViewHolderRowClick(this, item)
    }
}
