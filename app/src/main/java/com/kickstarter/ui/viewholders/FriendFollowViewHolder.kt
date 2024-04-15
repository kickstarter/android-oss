package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ActivityFriendFollowViewBinding
import com.kickstarter.ui.extensions.loadCircleImage

class FriendFollowViewHolder(private val binding: ActivityFriendFollowViewBinding) :
    ActivityListViewHolder(binding.root) {

    override fun onBind() {
        val context = context()
        val friend = activity().user() ?: return
        friend.avatar().small()?.let {
            binding.avatar.loadCircleImage(it)
        }
        // TODO: bold username
        binding.title.text = StringBuilder(friend.name())
            .append(" ")
            .append(context.getString(R.string.activity_friend_follow_is_following_you))
    }
}
