package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.MessageCenterTimestampLayoutBinding
import com.kickstarter.databinding.MessageViewBinding
import com.kickstarter.models.Message
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.ui.viewholders.MessageCenterTimestampViewHolder
import com.kickstarter.ui.viewholders.MessageViewHolder
import org.joda.time.DateTime

class MessagesAdapter : KSAdapter() {
    private fun getLayoutId(sectionRow: SectionRow): Int {
        if (objectFromSectionRow(sectionRow) is DateTime) {
            return R.layout.message_center_timestamp_layout
        } else if (objectFromSectionRow(sectionRow) is Message) {
            return R.layout.message_view
        }
        return R.layout.empty_view
    }

    fun messages(messages: List<Message>) {
        clearSections()

        // Group messages by start of day.
        messages
            .groupBy { it.createdAt().withTimeAtStartOfDay() }
            .forEach { dateAndMessages ->
                addSection(listOf(dateAndMessages.key))
                dateAndMessages.value.forEach { message -> addSection(listOf(message)) }
            }

        notifyDataSetChanged()
    }

    override fun layout(sectionRow: SectionRow): Int {
        return getLayoutId(sectionRow)
    }

    override fun onBindViewHolder(
        holder: KSViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)

        if (holder is MessageViewHolder) {
            // Let the MessageViewHolder know if it is the last position in the RecyclerView.
            holder.isLastPosition(position == itemCount - 1)
        }
    }

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return when (layout) {
            R.layout.message_center_timestamp_layout -> MessageCenterTimestampViewHolder(
                MessageCenterTimestampLayoutBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )

            R.layout.message_view -> MessageViewHolder(
                MessageViewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )

            else -> throw IllegalStateException("Invalid layout.")
        }
    }
}
