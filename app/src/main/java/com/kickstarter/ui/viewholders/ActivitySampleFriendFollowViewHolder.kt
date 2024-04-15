package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySampleFriendFollowViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Activity
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.viewmodels.ActivitySampleFriendFollowViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class ActivitySampleFriendFollowViewHolder(
    private val binding: ActivitySampleFriendFollowViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    private val ksString = requireNotNull(environment().ksString())
    private val vm: ActivitySampleFriendFollowViewHolderViewModel.ViewModel =
        ActivitySampleFriendFollowViewHolderViewModel.ViewModel()

    private val disposables = CompositeDisposable()

    interface Delegate {
        fun activitySampleFriendFollowViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendFollowViewHolder?)
    }

    init {
        this.vm.outputs.bindActivity()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                it.user()?.let { user ->
                    user.avatar().small().let { url ->
                        binding.activityImage.loadCircleImage(url)
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
            .addToDisposable(disposables)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        vm.inputs.configureWith(requireNotNull(data as Activity?) { Activity::class.java.toString() + " required to be non-null." })
    }

    private fun seeActivityOnClick() {
        delegate.activitySampleFriendFollowViewHolderSeeActivityClicked(this)
    }

    override fun destroy() {
        disposables.clear()
        vm.clear()
        super.destroy()
    }
}
