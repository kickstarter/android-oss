package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.SearchPopularTitleViewBinding

class PopularSearchTitleViewHolder(binding: SearchPopularTitleViewBinding) :
    KSViewHolder(binding.root) {
    init {
        binding.heading.setText(R.string.Popular_Projects)
    }
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        // no data to bind, this ViewHolder is just a static title
    }
}
