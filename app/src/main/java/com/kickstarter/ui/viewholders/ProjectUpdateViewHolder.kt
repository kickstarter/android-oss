package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectUpdateViewBinding
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Activity
import com.kickstarter.ui.extensions.loadImage
import org.joda.time.DateTime

class ProjectUpdateViewHolder(
    private val binding: ActivityProjectUpdateViewBinding,
    private val delegate: Delegate?
) : ActivityListViewHolder(binding.root) {

    private val ksString = requireNotNull(environment().ksString())

    interface Delegate {
        fun projectUpdateProjectClicked(viewHolder: ProjectUpdateViewHolder?, activity: Activity?)
        fun projectUpdateClicked(viewHolder: ProjectUpdateViewHolder?, activity: Activity?)
    }

    override fun onBind() {

        val context = context()
        val project = activity().project()
        val user = activity().user()
        val photo = project?.photo()
        val update = activity().update()
        if (project != null && user != null && photo != null && update != null) {
            val publishedAt = update.publishedAt() ?: DateTime()
            binding.projectName.text = project.name()
            binding.projectPhoto.loadImage(photo.little())
            binding.timestamp.text = DateTimeUtils.relative(context, ksString, publishedAt)
            binding.updateBody.text = update.truncatedBody()
            binding.updateSequence.text = ksString.format(
                context.getString(R.string.activity_project_update_update_count),
                "update_count", update.sequence().toString()
            )
            binding.updateTitle.text = update.title()
            binding.projectInfo.setOnClickListener { this.projectOnClick() }
            binding.updateInfo.setOnClickListener { this.updateOnClick() }
        }
    }

    private fun projectOnClick() {
        delegate?.projectUpdateProjectClicked(this, activity())
    }

    private fun updateOnClick() {
        delegate?.projectUpdateClicked(this, activity())
    }
}
