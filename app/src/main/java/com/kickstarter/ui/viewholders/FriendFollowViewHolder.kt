package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ActivityFriendFollowViewBinding
import com.kickstarter.libs.transformations.CircleTransformation
import com.squareup.picasso.Picasso

class FriendFollowViewHolder(private val binding: ActivityFriendFollowViewBinding) :
    ActivityListViewHolder(binding.root) {

    override fun onBind() {
        val context = context()
        val friend = activity().user() ?: return
        Picasso.with(context)
            .load(friend.avatar().small())
            .transform(CircleTransformation())
            .into(binding.avatar)

        // TODO: bold username
        binding.title.text = StringBuilder(friend.name())
            .append(" ")
            .append(context.getString(R.string.activity_friend_follow_is_following_you))
    }
}
