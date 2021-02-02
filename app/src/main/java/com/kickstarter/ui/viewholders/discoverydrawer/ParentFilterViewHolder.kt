package com.kickstarter.ui.viewholders.discoverydrawer

import android.view.View
import com.kickstarter.databinding.DiscoveryDrawerParentFilterViewBinding
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.KSViewHolder

class ParentFilterViewHolder(
    private val binding: DiscoveryDrawerParentFilterViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private var item: NavigationDrawerData.Section.Row? = null

    interface Delegate {
        fun parentFilterViewHolderRowClick(viewHolder: ParentFilterViewHolder, row: NavigationDrawerData.Section.Row)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        item = ObjectUtils.requireNonNull(data as NavigationDrawerData.Section.Row?, NavigationDrawerData.Section.Row::class.java)
    }

    override fun onBind() {
        val context = context()
        binding.filterTextView.text = item?.params()?.filterString(context, environment().ksString(), false, true)
        if (item?.rootIsExpanded() == true) {
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

    fun rowClick() {
        delegate.parentFilterViewHolderRowClick(this, item!!)
    }
}
