package com.kickstarter.ui.viewholders

import androidx.viewbinding.ViewBinding

class EmptyViewHolder(binding: ViewBinding) : KSViewHolder(binding.root) {
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
    }

    override fun onBind() {}
}
