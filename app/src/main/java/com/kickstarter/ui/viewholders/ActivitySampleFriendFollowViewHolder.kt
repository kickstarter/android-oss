package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendFollowViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Activity
import com.kickstarter.viewmodels.ActivitySampleFriendFollowViewHolderViewModel
import com.squareup.picasso.Picasso

class ActivitySampleFriendFollowViewHolder(
    private val binding: ActivitySampleFriendFollowViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private val ksString = environment().ksString()
    private val vm: ActivitySampleFriendFollowViewHolderViewModel.ViewModel =
        ActivitySampleFriendFollowViewHolderViewModel.ViewModel(environment())

    interface Delegate {
        fun activitySampleFriendFollowViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendFollowViewHolder?)
    }

    init {
        this.vm.outputs.bindActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                it.user()?.let { user ->
                    user.avatar()?.small()?.let {
                        Picasso.get().load(it)
                            .transform(CircleTransformation())
                            .into(binding.activityImage)
                    }

                    binding.activityTitle.text = ksString.format(
                        context().getString(R.string.activity_user_name_is_now_following_you),
                        "user_name",
                        user.name()
                    )
                    binding.activitySubtitle.setText(R.string.activity_follow_back)

                    // temp until followable :
                    binding.activitySubtitle.visibility = View.GONE
                    binding.seeActivityButton.setOnClickListener { seeActivityOnClick() }
                }
            }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        vm.inputs.configureWith(ObjectUtils.requireNonNull(data as Activity?, Activity::class.java))
    }

    private fun seeActivityOnClick() {
        delegate.activitySampleFriendFollowViewHolderSeeActivityClicked(this)
    }
}
