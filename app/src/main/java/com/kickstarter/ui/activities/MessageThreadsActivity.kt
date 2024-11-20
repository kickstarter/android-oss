package com.kickstarter.ui.activities

import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.MessageThreadsLayoutBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ToolbarUtils.fadeToolbarTitleOnExpand
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.wrapInParentheses
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.adapters.MessageThreadsAdapter
import com.kickstarter.ui.data.Mailbox
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.MessageThreadsViewModel.Factory
import com.kickstarter.viewmodels.MessageThreadsViewModel.MessageThreadsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MessageThreadsActivity : AppCompatActivity() {
    private lateinit var adapter: MessageThreadsAdapter
    private lateinit var ksString: KSString
    private lateinit var recyclerViewPaginator: RecyclerViewPaginatorV2
    private lateinit var binding: MessageThreadsLayoutBinding

    private val disposables = CompositeDisposable()

    private lateinit var viewModelFactory: Factory
    private val viewModel: MessageThreadsViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MessageThreadsLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        setUpAdapter()

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, intent = intent)
            env
        }
        ksString = requireNotNull(environment?.ksString())

        binding.messageThreadsRecyclerView.adapter = adapter
        binding.messageThreadsRecyclerView.layoutManager = LinearLayoutManager(this)

        recyclerViewPaginator = RecyclerViewPaginatorV2(
            binding.messageThreadsRecyclerView,
            { viewModel.inputs.nextPage() },
            viewModel.outputs.isFetchingMessageThreads()
        )

        binding.messageThreadsSwipeRefreshLayout.setOnRefreshListener {
            viewModel.inputs.swipeRefresh()
        }

        viewModel.outputs.isFetchingMessageThreads()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                // - Hides loading spinner from SwipeRefreshLayout according to isFetchingUpdates
                binding.messageThreadsSwipeRefreshLayout.isRefreshing = it
            }
            .addToDisposable(disposables)

        fadeToolbarTitleOnExpand(binding.messageThreadsAppBarLayout, binding.messageThreadsToolbar.messageThreadsCollapsedToolbarTitle)

        viewModel.outputs.mailboxTitle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setMailboxStrings(it) }
            .addToDisposable(disposables)

        viewModel.outputs.hasNoMessages()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.unreadCountTextView.text = getString(R.string.No_messages) }
            .addToDisposable(disposables)

        viewModel.outputs.hasNoUnreadMessages()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.unreadCountTextView.text = getString(R.string.No_unread_messages) }
            .addToDisposable(disposables)

        viewModel.outputs.messageThreadList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapter.messageThreads(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.unreadCountTextViewColorInt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.unreadCountTextView.setTextColor(ContextCompat.getColor(this, it)) }
            .addToDisposable(disposables)

        viewModel.outputs.unreadCountTextViewTypefaceInt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.unreadCountTextView.typeface = Typeface.create(getString(R.string.font_family_sans_serif), it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.unreadCountToolbarTextViewIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ViewUtils.setGone(binding.messageThreadsToolbar.messageThreadsToolbarUnreadCountTextView, it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.unreadMessagesCount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setUnreadTextViewText(it) }
            .addToDisposable(disposables)

        viewModel.outputs.unreadMessagesCountIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.setGone(binding.unreadCountTextView, it) }
            .addToDisposable(disposables)

        viewModel.outputs.isFetchingMessageThreads()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageThreadsSwipeRefreshLayout.isRefreshing = it }
            .addToDisposable(disposables)

        binding.switchMailboxButton.setOnClickListener {
            mailboxSwitchClicked()
        }

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    private fun mailboxSwitchClicked() = viewModel.inputs.mailbox(
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
