package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendFollowViewBinding
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Activity
import com.squareup.picasso.Picasso

class ActivitySampleFriendFollowViewHolder(
    private val binding: ActivitySampleFriendFollowViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {
    private var activity: Activity? = null
    private val ksString = environment().ksString()

    interface Delegate {
        fun activitySampleFriendFollowViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendFollowViewHolder)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        activity = ObjectUtils.requireNonNull(data as Activity?, Activity::class.java)
    }

    override fun onBind() {
        val context = context()
        activity?.user()?.let {user->
            Picasso.with(context).load(
                user.avatar()
                    .small()
            )
                .transform(CircleTransformation())
                .into(binding.activityImage)
            binding.activityTitle.text = ksString.format(context().getString(R.string.activity_user_name_is_now_following_you), "user_name", user.name())
            binding.activitySubtitle.setText(R.string.activity_follow_back)

            // temp until followable :
            binding.activitySubtitle.visibility = View.GONE
            binding.seeActivityButton.setOnClickListener { seeActivityOnClick() }
        }
    }

    fun seeActivityOnClick() {
        delegate.activitySampleFriendFollowViewHolderSeeActivityClicked(this)
    }
}
