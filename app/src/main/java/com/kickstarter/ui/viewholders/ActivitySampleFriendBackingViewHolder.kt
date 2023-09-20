package com.kickstarter.ui.viewholders

import android.text.Html
import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendBackingViewBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.viewmodels.ActivitySampleFriendBackingViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ActivitySampleFriendBackingViewHolder(
    private val binding: ActivitySampleFriendBackingViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private lateinit var vm: ActivitySampleFriendBackingViewHolderViewModel.ActivitySampleFriendBackingViewHolderViewModel
    private val env = this.context().getEnvironment()?.let { env ->
        vm = ActivitySampleFriendBackingViewHolderViewModel.ActivitySampleFriendBackingViewHolderViewModel(env)
        env
    }
    private val disposables = CompositeDisposable()
    private val ksString = requireNotNull(env?.ksString())
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        vm.inputs.configureWith(requireNotNull(data as Activity?) { Activity::class.java.toString() + "  required to be non-null." })
    }

    init {
        this.vm.outputs.bindActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { activity ->
                val user = activity?.user()
                val project = activity?.project()
                if (user != null && project != null) {
                    binding.activityTitle.visibility = View.GONE
                    user.avatar().small()?.let { url ->
                        binding.activityImage.loadCircleImage(url)
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
            .addToDisposable(disposables)
    }

    fun seeActivityOnClick() {
        delegate.activitySampleFriendBackingViewHolderSeeActivityClicked(this)
    }

    fun activityProjectOnClick(activity: Activity) {
        delegate.activitySampleFriendBackingViewHolderProjectClicked(this, activity.project())
    }

    override fun destroy() {
        this.vm.clearDisposables()
        disposables.clear()
        super.destroy()
    }

    interface Delegate {
        fun activitySampleFriendBackingViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendBackingViewHolder?)
        fun activitySampleFriendBackingViewHolderProjectClicked(viewHolder: ActivitySampleFriendBackingViewHolder, project: Project?)
    }
}
