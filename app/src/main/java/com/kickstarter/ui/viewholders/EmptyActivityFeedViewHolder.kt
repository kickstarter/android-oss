package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.databinding.EmptyActivityFeedViewBinding
import com.kickstarter.libs.utils.BooleanUtils

class EmptyActivityFeedViewHolder(
    private val binding: EmptyActivityFeedViewBinding,
    private val delegate: Delegate?
) : KSViewHolder(binding.root) {

    private var isLoggedIn = false

    interface Delegate {
        fun emptyActivityFeedDiscoverProjectsClicked(viewHolder: EmptyActivityFeedViewHolder?)
        fun emptyActivityFeedLoginClicked(viewHolder: EmptyActivityFeedViewHolder?)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        isLoggedIn = BooleanUtils.isTrue(data as Boolean?)
    }

    override fun onBind() {
        if (isLoggedIn) {
            binding.discoverProjectsButton.visibility = View.VISIBLE
            binding.loginButton.visibility = View.GONE
        } else {
            binding.discoverProjectsButton.visibility = View.GONE
            binding.loginButton.visibility = View.VISIBLE
        }
        binding.loginButton.setOnClickListener { loginOnClick() }
        binding.discoverProjectsButton.setOnClickListener { discoverProjectsOnClick() }
    }

    private fun discoverProjectsOnClick() {
        delegate?.emptyActivityFeedDiscoverProjectsClicked(this)
    }

    private fun loginOnClick() {
        delegate?.emptyActivityFeedLoginClicked(this)
    }
}
