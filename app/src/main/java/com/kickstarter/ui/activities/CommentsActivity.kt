package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.loadmore.PaginationHandler
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.Comment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.CommentsAdapter
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.CommentsViewModel
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CommentsViewModel.ViewModel::class)
class CommentsActivity :
    BaseActivity<CommentsViewModel.ViewModel>(),
    CommentsAdapter.Delegate {
    private lateinit var binding: ActivityCommentsLayoutBinding
    private val adapter = CommentsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsLayoutBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        binding.commentsRecyclerView.adapter = adapter

        setupPagination()

        viewModel.outputs.commentsList()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                comments ->
                adapter.takeData(comments)
            }

        viewModel.outputs.currentUserAvatar()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentComposer.setAvatarUrl(it)
            }

        viewModel.outputs.commentComposerStatus()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentComposer.setCommentComposerStatus(it)
            }

        viewModel.outputs.showCommentComposer()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentComposer.isVisible = it
            }

        viewModel.outputs.setEmptyState()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::setEmptyState)

        viewModel.outputs.insertComment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapter.insertData(it, 0)
                binding.commentsRecyclerView.scrollToPosition(0)
                viewModel.inputs.postCommentToServer(it)
            }

        viewModel.outputs.updateFailedComment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapter.update(it, 0)
                binding.commentsRecyclerView.scrollToPosition(0)
            }

        binding.commentComposer.setCommentComposerActionClickListener(object : OnCommentComposerViewClickedListener {
            override fun onClickActionListener(string: String) {
                postComment(string)
            }
        })
    }

    private fun setupPagination() {
        val paginationHandler = PaginationHandler(
            adapter,
            binding.commentsRecyclerView,
            binding.commentsSwipeRefreshLayout
        )

        paginationHandler.onRefreshListener = {
            viewModel.inputs.refresh()
        }

        paginationHandler.onLoadMoreListener = {
            viewModel.inputs.nextPage()
        }

        viewModel.outputs.enablePagination()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                paginationHandler.loadMoreEnabled = it
            }

        viewModel.outputs.isLoadingMoreItems()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                paginationHandler.isLoading(it)
                binding.commentsLoadingIndicator.isVisible = it
            }

        viewModel.outputs.isRefreshing()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                paginationHandler.refreshing(it)
            }
    }

    fun postComment(comment: String) {
        this.viewModel.inputs.postComment(comment, DateTime.now())
        this.binding.commentComposer.clearCommentComposer()
    }

    private fun setEmptyState(visibility: Boolean) {
        binding.commentsSwipeRefreshLayout.visibility = when (visibility) {
            true -> View.GONE
            else -> View.VISIBLE
        }

        binding.noComments.visibility = when (visibility) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    override fun emptyCommentsLoginClicked(viewHolder: EmptyCommentsViewHolder?) {
    }

    override fun onRetryViewClicked(comment: Comment) {
    }

    override fun onReplyButtonClicked(comment: Comment) {
        startThreadActivity(comment, true)
    }

    override fun onFlagButtonClicked(comment: Comment) {
    }

    override fun onCommentGuideLinesClicked(comment: Comment) {
    }

    override fun onCommentRepliesClicked(comment: Comment) {
        startThreadActivity(comment, false)
    }

    /**
     * Start the Thread activity with
     * @param comment the selected comment to reply
     * @param openKeyboard
     *      true: he focus needs to be on the composer view and set the keyboard open when open the activity
     *      false: in case we just need to open the replies screen
     *
     * // TODO: Once the viewReplies UI is completed call this method with openKeyboard = false
     * // TODO: https://kickstarter.atlassian.net/browse/NT-1955
     */
    private fun startThreadActivity(comment: Comment, openKeyboard: Boolean) {
        val threadIntent = Intent(this, ThreadActivity::class.java).apply {
            putExtra(IntentKey.COMMENT, comment)
            putExtra(IntentKey.REPLY_EXPAND, openKeyboard)
        }

        startActivityWithTransition(
            threadIntent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.commentsRecyclerView.adapter = null
    }
}
