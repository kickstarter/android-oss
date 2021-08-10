package com.kickstarter.ui.viewholders.discoverydrawer

import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.kickstarter.databinding.DiscoveryDrawerLoggedInViewBinding
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.LoggedInViewHolderViewModel
import com.squareup.picasso.Picasso

class LoggedInViewHolder(private val binding: DiscoveryDrawerLoggedInViewBinding, @NonNull private val delegate: Delegate) : KSViewHolder(binding.root) {
    private val viewModel: LoggedInViewHolderViewModel.ViewModel = LoggedInViewHolderViewModel.ViewModel(environment())

    interface Delegate {
        fun loggedInViewHolderActivityClick(@NonNull viewHolder: LoggedInViewHolder)
        fun loggedInViewHolderDashboardClick(@NonNull viewHolder: LoggedInViewHolder)
        fun loggedInViewHolderInternalToolsClick(@NonNull viewHolder: LoggedInViewHolder)
        fun loggedInViewHolderMessagesClick(@NonNull viewHolder: LoggedInViewHolder)
        fun loggedInViewHolderProfileClick(@NonNull viewHolder: LoggedInViewHolder, @NonNull user: User)
        fun loggedInViewHolderSettingsClick(@NonNull viewHolder: LoggedInViewHolder, @NonNull user: User)
    }

    init {

        this.viewModel.outputs.avatarUrl()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                Picasso.get()
                    .load(it)
                    .transform(CircleTransformation())
                    .into(binding.userImageView)
            }

        this.viewModel.outputs.name()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.userNameTextView.text = it }

        this.viewModel.outputs.unreadMessagesCount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.unreadMessagesCount.text = when {
                    IntegerUtils.isNullOrZero(it) -> null
                    else -> NumberUtils.format(it)
                }
            }

        this.viewModel.outputs.activityCount()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                binding.unseenActivityCount.text = when {
                    IntegerUtils.isNullOrZero(it) -> null
                    else -> NumberUtils.format(it)
                }
            }

        this.viewModel.outputs.activityCountTextColor()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.unseenActivityCount.setTextColor(ContextCompat.getColor(context(), it)) }

        this.viewModel.outputs.dashboardRowIsGone()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(binding.drawerDashboard, it) }

        this.viewModel.outputs.user()
            .subscribe { user ->
                binding.drawerSettings.setOnClickListener { this.delegate.loggedInViewHolderSettingsClick(this, user) }
                binding.drawerProfile.setOnClickListener { this.delegate.loggedInViewHolderProfileClick(this, user) }
                binding.userContainer.setOnClickListener { this.delegate.loggedInViewHolderProfileClick(this, user) }
            }

        binding.drawerActivity.setOnClickListener { this.delegate.loggedInViewHolderActivityClick(this) }
        binding.drawerDashboard.setOnClickListener { this.delegate.loggedInViewHolderDashboardClick(this) }
        binding.drawerMessages.setOnClickListener { this.delegate.loggedInViewHolderMessagesClick(this) }
        binding.internalTools.internalTools.setOnClickListener { this.delegate.loggedInViewHolderInternalToolsClick(this) }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        this.viewModel.inputs.configureWith(requireNotNull(data as User))
    }
}
