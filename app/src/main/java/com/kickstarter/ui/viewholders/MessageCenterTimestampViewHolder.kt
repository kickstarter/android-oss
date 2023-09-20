package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.MessageCenterTimestampLayoutBinding
import com.kickstarter.libs.utils.DateTimeUtils
import org.joda.time.DateTime

class MessageCenterTimestampViewHolder(private val binding: MessageCenterTimestampLayoutBinding) :
    KSViewHolder(binding.root) {

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val dateTime = requireNotNull(data as? DateTime?)
        binding.messageCenterTimestampTextView.text = DateTimeUtils.longDate(dateTime)
    }
}
