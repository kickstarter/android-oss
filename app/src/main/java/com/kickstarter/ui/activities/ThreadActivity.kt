package com.kickstarter.ui.activities

import android.os.Bundle
import android.util.Pair
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivityThreadLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.Comment
import com.kickstarter.ui.adapters.RepliesAdapter
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.viewholders.PaginationErrorViewHolder
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.ThreadViewModel
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

@RequiresActivityViewModel(ThreadViewModel.ViewModel::class)
class ThreadActivity :
    BaseActivity<ThreadViewModel.ViewModel>(),
    PaginationErrorViewHolder.ViewListener,
    RepliesAdapter.Delegate {

    private lateinit var binding: ActivityThreadLayoutBinding
    private lateinit var ksString: KSString

    private val adapter = RepliesAdapter(this)
    private val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
    private lateinit var recyclerViewPaginator: RecyclerViewPaginator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThreadLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ksString = environment().ksString()
        recyclerViewPaginator = RecyclerViewPaginator(binding.commentRepliesRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingReplies(), false)
        linearLayoutManager.stackFromEnd = true
        binding.commentRepliesRecyclerView.adapter = adapter
        binding.commentRepliesRecyclerView.layoutManager = linearLayoutManager

        this.viewModel.outputs.getRootComment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { comment ->
                adapter.updateRootCommentCell(comment)
            }

        this.viewModel.outputs
            .onCommentReplies()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.adapter.takeData(it.first.reversed(), it.second)
            }

        viewModel.outputs.shouldShowPaginationErrorUI()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapter.addErrorPaginationCell(it)
            }

        viewModel.outputs.isFetchingReplies()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.repliesLoadingIndicator.isVisible = it
            }

        this.viewModel.shouldFocusOnCompose()
            .delay(30, TimeUnit.MILLISECONDS)
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { shouldOpenKeyboard ->
                binding.replyComposer.requestCommentComposerKeyBoard(shouldOpenKeyboard)
            }

        viewModel.outputs.currentUserAvatar()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.setAvatarUrl(it)
            }

        viewModel.outputs.replyComposerStatus()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.setCommentComposerStatus(it)
            }

        viewModel.outputs.scrollToBottom()
            .compose(bindToLifecycle())
            .delay(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentRepliesRecyclerView.smoothScrollToPosition(0)
            }

        viewModel.outputs.showReplyComposer()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.isVisible = it
            }

        viewModel.outputs.loadMoreReplies()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                recyclerViewPaginator.reload()
            }

        binding.replyComposer.setCommentComposerActionClickListener(object :
                OnCommentComposerViewClickedListener {
                override fun onClickActionListener(string: String) {
                    postReply(string)
                    hideKeyboard()
                }
            })
    }

    fun postReply(comment: String) {
        this.viewModel.inputs.insertNewReplyToList(comment, DateTime.now())
        this.binding.replyComposer.clearCommentComposer()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    override fun onRetryViewClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onReplyButtonClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onFlagButtonClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onCommentGuideLinesClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onCommentRepliesClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onCommentPostedSuccessFully(comment: Comment) {
    }

    override fun loadMoreCallback() {
        viewModel.inputs.onViewMoreClicked()
    }

    override fun retryCallback() {
        viewModel.inputs.onViewMoreClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.commentRepliesRecyclerView.adapter = null
        this.viewModel = null
    }
}
