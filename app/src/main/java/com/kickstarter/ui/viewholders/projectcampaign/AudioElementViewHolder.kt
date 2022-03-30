package com.kickstarter.ui.viewholders.projectcampaign

import com.kickstarter.databinding.ViewElementAudioFromHtmlBinding
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.projectpage.AudioViewElementViewHolderViewModel

class AudioElementViewHolder(
    val binding: ViewElementAudioFromHtmlBinding
) : KSViewHolder(binding.root) {

    private var viewModel = AudioViewElementViewHolderViewModel.ViewModel(environment())

    override fun bindData(data: Any?) {
        (data as? AudioViewElement)?.run {
            viewModel.inputs.configureWith(this)
        }
    }
}
