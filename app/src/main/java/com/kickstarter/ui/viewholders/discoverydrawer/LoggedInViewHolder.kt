package com.kickstarter.ui.viewholders.discoverydrawer

import android.view.View
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.LoggedInViewHolderViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.discovery_drawer_logged_in_view.view.*

class LoggedInViewHolder(@NonNull view: View, @NonNull private val delegate: Delegate) : KSViewHolder(view) {
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
                            .into(view.user_image_view)
                }

        this.viewModel.outputs.name()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.user_name_text_view.text = it }

        this.viewModel.outputs.unreadMessagesCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    view.unread_messages_count.text = when {
                        IntegerUtils.isNullOrZero(it) -> null
                        else -> NumberUtils.format(it)
                    }
                }

        this.viewModel.outputs.activityCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    view.unseen_activity_count.text = when {
                        IntegerUtils.isNullOrZero(it) -> null
                        else -> NumberUtils.format(it)
                    }
                }

        this.viewModel.outputs.activityCountTextColor()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { view.unseen_activity_count.setTextColor(ContextCompat.getColor(context(), it)) }

        this.viewModel.outputs.dashboardRowIsGone()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { ViewUtils.setGone(view.drawer_dashboard, it) }

        this.viewModel.outputs.user()
                .subscribe { user ->
                    view.drawer_settings.setOnClickListener { this.delegate.loggedInViewHolderSettingsClick(this, user) }
                    view.drawer_profile.setOnClickListener { this.delegate.loggedInViewHolderProfileClick(this, user) }
                    view.user_container.setOnClickListener { this.delegate.loggedInViewHolderProfileClick(this, user) }
                }

        view.drawer_activity.setOnClickListener { this.delegate.loggedInViewHolderActivityClick(this) }
        view.drawer_dashboard.setOnClickListener { this.delegate.loggedInViewHolderDashboardClick(this) }
        view.drawer_messages.setOnClickListener { this.delegate.loggedInViewHolderMessagesClick(this) }
        view.internal_tools.setOnClickListener { this.delegate.loggedInViewHolderInternalToolsClick(this) }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        this.viewModel.inputs.configureWith(requireNotNull(data as User))
    }
}
