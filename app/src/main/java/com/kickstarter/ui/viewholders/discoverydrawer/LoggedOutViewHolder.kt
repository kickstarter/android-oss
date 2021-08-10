package com.kickstarter.ui.viewholders.discoverydrawer

import com.kickstarter.databinding.DiscoveryDrawerLoggedOutViewBinding
import com.kickstarter.ui.viewholders.KSViewHolder

class LoggedOutViewHolder(private val binding: DiscoveryDrawerLoggedOutViewBinding, private val delegate: Delegate) : KSViewHolder(binding.root) {
    interface Delegate {
        fun loggedOutViewHolderActivityClick(viewHolder: LoggedOutViewHolder)
        fun loggedOutViewHolderInternalToolsClick(viewHolder: LoggedOutViewHolder)
        fun loggedOutViewHolderLoginToutClick(viewHolder: LoggedOutViewHolder)
        fun loggedOutViewHolderHelpClick(viewHolder: LoggedOutViewHolder)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
    }

    override fun onBind() {
        binding.drawerActivity.setOnClickListener {
            activityClick()
        }
        binding.drawerHelp.setOnClickListener {
            helpClick()
        }
        binding.internalToolsLayout.internalTools.setOnClickListener {
            internalToolsClick()
        }
        binding.loggedOutTextView.setOnClickListener {
            loginToutClick()
        }
    }

    private fun activityClick() {
        delegate.loggedOutViewHolderActivityClick(this)
    }

    private fun helpClick() {
        delegate.loggedOutViewHolderHelpClick(this)
    }

    private fun internalToolsClick() {
        delegate.loggedOutViewHolderInternalToolsClick(this)
    }

    private fun loginToutClick() {
        delegate.loggedOutViewHolderLoginToutClick(this)
    }
}
