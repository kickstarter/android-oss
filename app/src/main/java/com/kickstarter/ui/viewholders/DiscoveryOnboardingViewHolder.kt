package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.DiscoveryOnboardingViewBinding

class DiscoveryOnboardingViewHolder(
    private val binding: DiscoveryOnboardingViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    interface Delegate {
        fun discoveryOnboardingViewHolderLoginToutClick(viewHolder: DiscoveryOnboardingViewHolder?)
    }

    override fun onBind() {
        binding.loginToutButton.setOnClickListener {
            loginToutClick()
        }
    }

    override fun bindData(data: Any?) {
    }

    private fun loginToutClick() {
        delegate.discoveryOnboardingViewHolderLoginToutClick(this)
    }
}
