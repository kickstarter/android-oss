package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectStateChangedViewBinding
import com.kickstarter.libs.KSString
import com.kickstarter.models.Activity
import com.squareup.picasso.Picasso

class ProjectStateChangedViewHolder(
    private val binding: ActivityProjectStateChangedViewBinding,
    private val delegate: Delegate?
) : ActivityListViewHolder(binding.root) {

    private val ksString: KSString = environment().ksString()

    interface Delegate {
        fun projectStateChangedClicked(viewHolder: ProjectStateChangedViewHolder?, activity: Activity?)
    }

    override fun onBind() {
        val project = activity().project()
        val user = activity().user()
        val photo = project?.photo()
        if (project != null && user != null && photo != null) {
            Picasso.with(context())
                .load(photo.little())
                .into(binding.projectPhoto)

            val title = when (activity().category()) {
                Activity.CATEGORY_FAILURE -> context().getString(R.string.activity_project_state_change_project_was_not_successfully_funded)
                Activity.CATEGORY_CANCELLATION -> context().getString(R.string.activity_project_state_change_project_was_cancelled_by_creator)
                Activity.CATEGORY_SUSPENSION -> context().getString(R.string.activity_project_state_change_project_was_suspended)
                else -> ""
            }

            binding.title.text = ksString.format(title, "project_name", project.name())

            binding.cardView.setOnClickListener {
                stateChangeCardClick()
            }
        }
    }

    private fun stateChangeCardClick() {
        delegate?.projectStateChangedClicked(this, activity())
    }
}
