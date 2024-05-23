package com.kickstarter.ui.viewholders.discoverydrawer

import androidx.core.content.ContextCompat
import com.kickstarter.databinding.DiscoveryDrawerLoggedInViewBinding
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.User
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.LoggedInViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class LoggedInViewHolder(
    private val binding: DiscoveryDrawerLoggedInViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel: LoggedInViewHolderViewModel.ViewModel = LoggedInViewHolderViewModel.ViewModel(environment())

    private val disposables = CompositeDisposable()
    interface Delegate {
        fun loggedInViewHolderActivityClick(viewHolder: LoggedInViewHolder)
        fun loggedInViewHolderInternalToolsClick(viewHolder: LoggedInViewHolder)
        fun loggedInViewHolderMessagesClick(viewHolder: LoggedInViewHolder)
        fun loggedInViewHolderProfileClick(viewHolder: LoggedInViewHolder, user: User)
        fun loggedInViewHolderSettingsClick(viewHolder: LoggedInViewHolder, user: User)
        fun loggedInViewHolderPledgedProjectsClick(viewHolder: LoggedInViewHolder)
    }

    init {

        this.viewModel.outputs.avatarUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.userImageView.loadCircleImage(it)
            }.addToDisposable(disposables)

        this.viewModel.outputs.name()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.userNameTextView.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.unreadMessagesCount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.unreadMessagesCount.text = when {
                    it.isNullOrZero() -> null
                    else -> NumberUtils.format(it)
                }
            }.addToDisposable(disposables)

        this.viewModel.outputs.activityCount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.unseenActivityCount.text = when {
                    it.isNullOrZero() -> null
                    else -> NumberUtils.format(it)
                }
            }.addToDisposable(disposables)

        this.viewModel.outputs.pledgedProjectsIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.pledgedProjectsOverview.visibility = it.toVisibility() }
            .addToDisposable(disposables)

        this.viewModel.outputs.activityCountTextColor()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.unseenActivityCount.setTextColor(ContextCompat.getColor(context(), it)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.user()
            .subscribe { user ->
                binding.drawerSettings.setOnClickListener { this.delegate.loggedInViewHolderSettingsClick(this, user) }
                binding.drawerProfile.setOnClickListener { this.delegate.loggedInViewHolderProfileClick(this, user) }
                binding.userContainer.setOnClickListener { this.delegate.loggedInViewHolderProfileClick(this, user) }
                binding.pledgedProjectsOverview.setOnClickListener { this.delegate.loggedInViewHolderPledgedProjectsClick(this) }
            }.addToDisposable(disposables)

        binding.drawerActivity.setOnClickListener { this.delegate.loggedInViewHolderActivityClick(this) }
        binding.drawerMessages.setOnClickListener { this.delegate.loggedInViewHolderMessagesClick(this) }
        binding.internalTools.internalTools.setOnClickListener { this.delegate.loggedInViewHolderInternalToolsClick(this) }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        this.viewModel.inputs.configureWith(requireNotNull(data as User))
    }

    override fun destroy() {
        disposables.clear()
        viewModel.inputs.onCleared()
        super.destroy()
    }
}
