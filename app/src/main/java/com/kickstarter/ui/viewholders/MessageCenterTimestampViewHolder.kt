package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.MessageCenterTimestampLayoutBinding
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ObjectUtils
import org.joda.time.DateTime

class MessageCenterTimestampViewHolder(private val binding: MessageCenterTimestampLayoutBinding) :
    KSViewHolder(binding.root) {

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val dateTime = ObjectUtils.requireNonNull(data as? DateTime?)
        binding.messageCenterTimestampTextView.text = DateTimeUtils.longDate(dateTime)
    }
}
