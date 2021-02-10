package com.kickstarter.ui.viewholders

import android.text.Html
import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendBackingViewBinding
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.squareup.picasso.Picasso

class ActivitySampleFriendBackingViewHolder(
    private val binding: ActivitySampleFriendBackingViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private val ksString = environment().ksString()
    private var activity: Activity? = null
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        activity = ObjectUtils.requireNonNull(data as Activity?, Activity::class.java)
    }

    override fun onBind() {
        val context = context()
        val user = activity?.user()
        val project = activity?.project()
        if (user != null && project != null) {
            binding.activityTitle.visibility = View.GONE
            user.avatar().small()?.let {
                Picasso.with(context).load(it)
                    .transform(CircleTransformation())
                    .into(binding.activityImage)
            }
            binding.activitySubtitle.text = Html.fromHtml(
                ksString.format(
                    context().getString(R.string.activity_friend_backed_project_name_by_creator_name),
                    "friend_name", user.name(),
                    "project_name", project.name(),
                    "creator_name", project.creator().name()
                )
            )
        }
        binding.seeActivityButton.setOnClickListener {
            seeActivityOnClick()
        }
        binding.activityClickArea.setOnClickListener {
            activityProjectOnClick()
        }
    }

    fun seeActivityOnClick() {
        delegate.activitySampleFriendBackingViewHolderSeeActivityClicked(this)
    }

    fun activityProjectOnClick() {
        delegate.activitySampleFriendBackingViewHolderProjectClicked(this, activity?.project())
    }

    interface Delegate {
        fun activitySampleFriendBackingViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendBackingViewHolder)
        fun activitySampleFriendBackingViewHolderProjectClicked(viewHolder: ActivitySampleFriendBackingViewHolder, project: Project?)
    }
}
