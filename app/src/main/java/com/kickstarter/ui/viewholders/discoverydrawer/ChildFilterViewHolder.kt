package com.kickstarter.ui.viewholders.discoverydrawer

import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryDrawerChildFilterViewBinding
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.KSViewHolder
import timber.log.Timber

class ChildFilterViewHolder(
    private val binding: DiscoveryDrawerChildFilterViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private val ksString = environment().ksString()
    private var item: NavigationDrawerData.Section.Row? = null

    interface Delegate {
        fun childFilterViewHolderRowClick(viewHolder: ChildFilterViewHolder, row: NavigationDrawerData.Section.Row)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        item = ObjectUtils.requireNonNull(data as NavigationDrawerData.Section.Row?, NavigationDrawerData.Section.Row::class.java)
    }

    override fun onBind() {
        val context = context()
        val category = item?.params()?.category()
        if (category?.isRoot == true) {
            binding.filterTextView.text = item?.params()?.filterString(context, ksString)
        } else {
            binding.filterTextView.text = item?.params()?.filterString(context, ksString)
        }

        val textColor = if (item?.selected() == true)
            context.resources.getColor(R.color.accent, null)
        else
            context.resources.getColor(R.color.kds_support_700, null)

        val iconDrawable = if (item?.selected() == true)
            ResourcesCompat.getDrawable(context.resources, R.drawable.ic_label_green, null)
        else
            ResourcesCompat.getDrawable(context.resources, R.drawable.ic_label, null)

        val backgroundDrawable = if (item?.selected() == true)
            ResourcesCompat.getDrawable(context.resources, R.drawable.drawer_selected, null)
        else
            null

        binding.filterTextView.apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(iconDrawable, null, null, null)
            setTextColor(textColor)
            background = backgroundDrawable
            setOnClickListener {
                textViewClick()
            }
        }
    }

    fun textViewClick() {
        Timber.d("DiscoveryDrawerChildParamsViewHolder topFilterViewHolderRowClick")
        item?.let { delegate.childFilterViewHolderRowClick(this, it) }
    }
}
