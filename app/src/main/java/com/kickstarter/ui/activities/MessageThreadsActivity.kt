package com.kickstarter.ui.activities

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.MessageThreadsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.SwipeRefresher
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ToolbarUtils.fadeToolbarTitleOnExpand
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.wrapInParentheses
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.adapters.MessageThreadsAdapter
import com.kickstarter.ui.data.Mailbox
import com.kickstarter.viewmodels.MessageThreadsViewModel

@RequiresActivityViewModel(MessageThreadsViewModel.ViewModel::class)
class MessageThreadsActivity : BaseActivity<MessageThreadsViewModel.ViewModel>() {
    private lateinit var adapter: MessageThreadsAdapter
    private lateinit var ksString: KSString
    private lateinit var recyclerViewPaginator: RecyclerViewPaginator
    private lateinit var binding: MessageThreadsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MessageThreadsLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpAdapter()

        ksString = environment().ksString()

        binding.messageThreadsRecyclerView.adapter = adapter
        binding.messageThreadsRecyclerView.layoutManager = LinearLayoutManager(this)

        recyclerViewPaginator = RecyclerViewPaginator(binding.messageThreadsRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingMessageThreads)

        SwipeRefresher(
            this, binding.messageThreadsSwipeRefreshLayout, { viewModel.inputs.swipeRefresh() }
        ) { viewModel.outputs.isFetchingMessageThreads }

        fadeToolbarTitleOnExpand(binding.messageThreadsAppBarLayout, binding.messageThreadsToolbar.messageThreadsCollapsedToolbarTitle)

        viewModel.outputs.mailboxTitle()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setMailboxStrings(it) }

        viewModel.outputs.hasNoMessages()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.unreadCountTextView.text = getString(R.string.No_messages) }

        viewModel.outputs.hasNoUnreadMessages()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.unreadCountTextView.text = getString(R.string.No_unread_messages) }
        viewModel.outputs.messageThreadList()
            .compose(bindToLifecycle())
            .compose<List<MessageThread?>>(Transformers.observeForUI())
            .subscribe { adapter.messageThreads(it) }

        viewModel.outputs.unreadCountTextViewColorInt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.unreadCountTextView.setTextColor(ContextCompat.getColor(this, it)) }

        viewModel.outputs.unreadCountTextViewTypefaceInt()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.unreadCountTextView.typeface = Typeface.create(getString(R.string.font_family_sans_serif), it)
            }

        viewModel.outputs.unreadCountToolbarTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messageThreadsToolbar.messageThreadsToolbarUnreadCountTextView))

        viewModel.outputs.unreadMessagesCount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setUnreadTextViewText(it) }

        viewModel.outputs.unreadMessagesCountIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.setGone(binding.unreadCountTextView, it) }

        viewModel.outputs.isFetchingMessageThreads
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messageThreadsSwipeRefreshLayout.isRefreshing = it }

        binding.switchMailboxButton.setOnClickListener {
            mailboxSwitchClicked()
        }
    }

    fun mailboxSwitchClicked() = viewModel.inputs.mailbox(
        if (binding.mailboxTextView.text == this.getString(R.string.messages_navigation_inbox))
            Mailbox.SENT
        else
            Mailbox.INBOX
    )

    private fun setMailboxStrings(stringRes: Int) {
        val mailbox = getString(stringRes)
        binding.mailboxTextView.text = mailbox
        binding.messageThreadsToolbar.messageThreadsCollapsedToolbarMailboxTitle.text = mailbox
        binding.switchMailboxButton.text = if (mailbox == this.getString(R.string.messages_navigation_inbox)) getString(R.string.messages_navigation_sent) else this.getString(R.string.messages_navigation_inbox)
    }

    override fun exitTransition() = TransitionUtils.slideInFromLeft()

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.messageThreadsRecyclerView.adapter = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.inputs.onResume()
    }

    private fun setUnreadTextViewText(unreadCount: Int) {
        val unreadCountString = NumberUtils.format(unreadCount)
        binding.unreadCountTextView.text = ksString.format(getString(R.string.unread_count_unread), "unread_count", unreadCountString)
        binding.messageThreadsToolbar.messageThreadsToolbarUnreadCountTextView.text = unreadCountString.wrapInParentheses()
    }

    private fun setUpAdapter() {
        adapter = MessageThreadsAdapter(object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return threadsAreTheSame(oldItem, newItem)
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return threadsAreTheSame(oldItem, newItem)
            }

            private fun threadsAreTheSame(oldItem: Any, newItem: Any): Boolean {
                val oldThread = oldItem as MessageThread
                val newThread = newItem as MessageThread
                return oldThread.id() == newThread.id()
            }
        })
    }
}
