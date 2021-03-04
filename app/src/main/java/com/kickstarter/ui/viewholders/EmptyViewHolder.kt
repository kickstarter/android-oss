package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.EmptyViewBinding

class EmptyViewHolder(binding: EmptyViewBinding) : KSViewHolder(binding.root) {
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
    }

    override fun onBind() {}
}