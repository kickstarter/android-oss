package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleProjectViewBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.ui.extensions.loadImage
import com.kickstarter.viewmodels.ActivitySampleProjectViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ActivitySampleProjectViewHolder(
    private val binding: ActivitySampleProjectViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private val ksString = requireNotNull(environment().ksString())
    private val vm: ActivitySampleProjectViewHolderViewModel.ActivitySampleProjectHolderViewModel =
        ActivitySampleProjectViewHolderViewModel.ActivitySampleProjectHolderViewModel()
    private val disposables = CompositeDisposable()

    interface Delegate {
        fun activitySampleProjectViewHolderSeeActivityClicked(viewHolder: ActivitySampleProjectViewHolder?)
        fun activitySampleProjectViewHolderProjectClicked(viewHolder: ActivitySampleProjectViewHolder, project: Project?)
        fun activitySampleProjectViewHolderUpdateClicked(viewHolder: ActivitySampleProjectViewHolder?, activity: Activity)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        vm.inputs.configureWith(requireNotNull(data as Activity?) { Activity::class.java.toString() + " required to be non-null." })
    }

    init {

        val context = context()
        this.vm.outputs.bindActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { activity ->
                activity.project()?.let { project ->
                    val photo = project.photo()
                    photo?.let {
                        binding.activityImage.loadImage(it.little())
                    }
                    binding.activityTitle.text = project.name()
                    val activitySubtitleText = when (activity.category()) {
                        Activity.CATEGORY_FAILURE -> context.getString(R.string.activity_project_was_not_successfully_funded)
                        Activity.CATEGORY_CANCELLATION -> context.getString(R.string.activity_funding_canceled)
                        Activity.CATEGORY_LAUNCH ->
                            activity.user()?.let {
                                ksString.format(context.getString(R.string.activity_user_name_launched_project), "user_name", it.name())
                            }

                        Activity.CATEGORY_SUCCESS -> context.getString(R.string.activity_successfully_funded)
                        Activity.CATEGORY_UPDATE ->
                            activity.update()?.let { update ->
                                ksString.format(
                                    context.getString(R.string.activity_posted_update_number_title),
                                    "update_number", update.sequence().toString(),
                                    "update_title", update.title()
                                )
                            }

                        else -> ""
                    }
                    if (activitySubtitleText?.isNotBlank() == true) {
                        binding.activitySubtitle.text = activitySubtitleText
                    }
                }
                binding.seeActivityButton.setOnClickListener {
                    seeActivityOnClick()
                }
                binding.activityClickArea.setOnClickListener {
                    activityProjectOnClick(activity)
                }
            }.addToDisposable(disposables)
    }

    fun seeActivityOnClick() {
        delegate.activitySampleProjectViewHolderSeeActivityClicked(this)
    }

    fun activityProjectOnClick(activity: Activity) = if (activity.category() == Activity.CATEGORY_UPDATE) {
        delegate.activitySampleProjectViewHolderUpdateClicked(this, activity)
    } else {
        delegate.activitySampleProjectViewHolderProjectClicked(this, activity.project())
    }

    override fun destroy() {
        vm.inputs.onCleared()
        disposables.clear()
        super.destroy()
    }
}
