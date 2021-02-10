package com.kickstarter.ui.viewholders.discoverydrawer

import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryDrawerTopFilterViewBinding
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.KSViewHolder

class TopFilterViewHolder(
    private val binding: DiscoveryDrawerTopFilterViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {
    private var item: NavigationDrawerData.Section.Row? = null

    interface Delegate {
        fun topFilterViewHolderRowClick(viewHolder: TopFilterViewHolder, row: NavigationDrawerData.Section.Row)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        item = ObjectUtils.requireNonNull(data as NavigationDrawerData.Section.Row?, NavigationDrawerData.Section.Row::class.java)
    }

    override fun onBind() {
        val context = context()

        val textColor = when {
            item?.selected() == true ->
                context.resources.getColor(R.color.accent, null)
            else ->
                context.resources.getColor(R.color.kds_support_700, null)
        }

        val iconDrawable = when {
            item?.selected() == true ->
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_label_green, null)
            else ->
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_label, null)
        }

        val backgroundDrawable = when {
            item?.selected() == true ->
                ResourcesCompat.getDrawable(context.resources, R.drawable.drawer_selected, null)
            else -> null
        }

        binding.filterTextView.apply {
            text = item?.params()?.filterString(context, environment().ksString())
            setCompoundDrawablesRelativeWithIntrinsicBounds(iconDrawable, null, null, null)
            setTextColor(textColor)
            background = backgroundDrawable
            setOnClickListener {
                textViewClick()
            }
        }
    }

    private fun textViewClick() {
        item?.let { delegate.topFilterViewHolderRowClick(this, it) }
    }
}
