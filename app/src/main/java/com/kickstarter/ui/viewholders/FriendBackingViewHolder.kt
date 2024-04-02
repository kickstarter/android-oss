package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ActivityFriendBackingViewBinding
import com.kickstarter.libs.utils.SocialUtils
import com.kickstarter.models.Activity
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.loadImage

class FriendBackingViewHolder(
    private val binding: ActivityFriendBackingViewBinding,
    private val delegate: Delegate?
) : ActivityListViewHolder(binding.root) {
    private val ksString = requireNotNull(environment().ksString())

    interface Delegate {
        fun friendBackingClicked(viewHolder: FriendBackingViewHolder?, activity: Activity?)
    }

    override fun onBind() {
        val context = context()
        val activityUser = activity().user() ?: return
        val activityProject = activity().project() ?: return
        val projectCreator = activityProject.creator() ?: return
        val projectCategory = activityProject.category() ?: return
        val projectPhoto = activityProject.photo() ?: return
        activityUser.avatar().small()?.let {
            binding.avatar.loadCircleImage(it)
        }

        binding.creatorName.text = ksString.format(context.getString(R.string.project_creator_by_creator), "creator_name", projectCreator.name())
        binding.projectName.text = activityProject.name()
        binding.projectPhoto.loadImage(projectPhoto.little())
        binding.title.text = SocialUtils.friendBackingActivityTitle(
            context,
            activityUser.name(),
            projectCategory.rootId(),
            ksString
        )
        binding.friendBackingCardView.setOnClickListener {
            onClick()
        }
    }

    fun onClick() {
        delegate?.friendBackingClicked(this, activity())
    }
}
