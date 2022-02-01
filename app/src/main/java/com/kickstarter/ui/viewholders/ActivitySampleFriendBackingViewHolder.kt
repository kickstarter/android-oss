package com.kickstarter.ui.viewholders

import android.text.Html
import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendBackingViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.ActivitySampleFriendBackingViewHolderViewModel
import com.squareup.picasso.Picasso

class ActivitySampleFriendBackingViewHolder(
    private val binding: ActivitySampleFriendBackingViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private val ksString = environment().ksString()
    private val vm: ActivitySampleFriendBackingViewHolderViewModel.ViewModel =
        ActivitySampleFriendBackingViewHolderViewModel.ViewModel(environment())

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        vm.inputs.configureWith(ObjectUtils.requireNonNull(data as Activity?, Activity::class.java))
    }

    init {
        this.vm.outputs.bindActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { activity ->
                val user = activity?.user()
                val project = activity?.project()
                if (user != null && project != null) {
                    binding.activityTitle.visibility = View.GONE
                    user.avatar().small()?.let { url ->
                        Picasso.get().load(url)
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
                    activityProjectOnClick(activity)
                }
            }
    }

    fun seeActivityOnClick() {
        delegate.activitySampleFriendBackingViewHolderSeeActivityClicked(this)
    }

    fun activityProjectOnClick(activity: Activity) {
        delegate.activitySampleFriendBackingViewHolderProjectClicked(this, activity.project())
    }

    interface Delegate {
        fun activitySampleFriendBackingViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendBackingViewHolder)
        fun activitySampleFriendBackingViewHolderProjectClicked(viewHolder: ActivitySampleFriendBackingViewHolder, project: Project?)
    }
}
