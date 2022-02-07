package com.kickstarter.ui.viewholders.projectcampaign

import android.widget.TextView
import com.kickstarter.databinding.ViewElementHeaderBinding
import com.kickstarter.ui.viewholders.KSViewHolder

class HeaderElementViewHolder(
    val binding: ViewElementHeaderBinding
) : KSViewHolder(binding.root) {
    private val textView: TextView = binding.title

    fun configure(element: String) {
        textView.text = element
    }

    override fun bindData(data: Any?) {
        (data as? String).apply {
            this?.let { configure(it) }
        }
    }
}
