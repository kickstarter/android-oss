package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import com.kickstarter.R
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.SwipeRefresher
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.Comment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.CommentInitialErrorAdapter
import com.kickstarter.ui.adapters.CommentPaginationErrorAdapter
import com.kickstarter.ui.adapters.CommentsAdapter
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.CommentsViewModel
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

@RequiresActivityViewModel(CommentsViewModel.ViewModel::class)
class CommentsActivity :
    BaseActivity<CommentsViewModel.ViewModel>(),
    CommentsAdapter.Delegate,
    CommentPaginationErrorAdapter.Delegate {
    private lateinit var binding: ActivityCommentsLayoutBinding
    /** comments list and initial load error adapter **/
    private val commentsAdapter = CommentsAdapter(this)
    private val commentPaginationErrorAdapter = CommentPaginationErrorAdapter(this)
    private val commentInitialErrorAdapter = CommentInitialErrorAdapter()

    private lateinit var recyclerViewPaginator: RecyclerViewPaginator
    private lateinit var swipeRefresher: SwipeRefresher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsLayoutBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        /** use ConcatAdapter to bind adapters to recycler view and replace the section issue **/
        binding.commentsRecyclerView.adapter =
            ConcatAdapter(commentInitialErrorAdapter, commentsAdapter, commentPaginationErrorAdapter)

        binding.backButton.setOnClickListener {
            handleBackAction()
        }

        setupPagination()

        viewModel.outputs.commentsList()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                comments ->
                commentsAdapter.takeData(comments)
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

        viewModel.outputs.shouldShowInitialLoadErrorUI()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setEmptyState(false)
                commentInitialErrorAdapter.insertPageError(it)
            }

        /*
         * A little delay after new item is inserted
         * This is necessary for the scroll to take effect
         */
        viewModel.outputs.scrollToTop()
            .filter { it == true }
            .compose(bindToLifecycle())
            .delay(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentsRecyclerView.smoothScrollToPosition(0)
            }

        viewModel.outputs.closeCommentsPage()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                closeCommentsActivity()
            }

        viewModel.outputs.shouldShowPaginationErrorUI()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                commentPaginationErrorAdapter.addErrorPaginationCell(it)
            }

        binding.commentComposer.setCommentComposerActionClickListener(object : OnCommentComposerViewClickedListener {
            override fun onClickActionListener(string: String) {
                postComment(string)
                hideKeyboard()
            }
        })

        viewModel.outputs.showCommentGuideLinesLink()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ApplicationUtils.openUrlExternally(this, UrlUtils.appendPath(environment().webEndpoint(), COMMENT_KICKSTARTER_GUIDELINES))
            }

        viewModel.outputs.hasPendingComments()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.first) {
                    handleBackAction(it.second)
                } else {
                    executeActions(it.second)
                }
            }
    }

    private fun handleBackAction(isBackAction: Boolean) {
        this.showAlertDialog(
            getString(R.string.Your_comment_wasnt_posted),
            getString(R.string.You_will_lose_the_comment_if_you_leave_this_page),
            getString(R.string.Cancel),
            getString(R.string.Leave_page),
            false,
            positiveAction = {
                if (!isBackAction) {
                    binding.commentsSwipeRefreshLayout.isRefreshing = false
                }
            },
            negativeAction = {
                executeActions(isBackAction)
            }
        )
    }

    fun executeActions(isBackAction: Boolean) {
        if (!isBackAction) {
            viewModel.inputs.refresh()
        } else {
            viewModel.inputs.backPressed()
        }
    }

    override fun back() {
        handleBackAction()
    }

    private fun handleBackAction() {
        if (binding.commentComposer.isCommentComposerEmpty() == true) {
            viewModel.inputs.checkIfThereAnyPendingComments(true)
        } else {
            handleBackAction(true)
        }
    }

    private fun closeCommentsActivity() {
        super.back()
        this.finishActivity(taskId)
    }

    private fun setupPagination() {

        recyclerViewPaginator = RecyclerViewPaginator(binding.commentsRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingComments())

        swipeRefresher = SwipeRefresher(
            this, binding.commentsSwipeRefreshLayout,
            {
                viewModel.inputs.checkIfThereAnyPendingComments(false)
            }
        ) { viewModel.outputs.isFetchingComments() }

        viewModel.outputs.isFetchingComments()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentsLoadingIndicator.isVisible = it
            }

        viewModel.outputs.startThreadActivity()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startThreadActivity(it.first, it.second)
            }

        viewModel.outputs.startThreadActivityFromDeepLink()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startThreadActivityFromDeepLink(it)
            }
    }

    fun postComment(comment: String) {
        this.viewModel.inputs.insertNewCommentToList(comment, DateTime.now())
        this.binding.commentComposer.clearCommentComposer()
    }

    private fun setEmptyState(visibility: Boolean) {
        binding.commentsSwipeRefreshLayout.visibility = when (visibility) {
            true -> View.INVISIBLE
            else -> View.VISIBLE
        }

        binding.noComments.visibility = when (visibility) {
            true -> View.VISIBLE
            else -> View.GONE
        }
        binding.commentsSwipeRefreshLayout.visibility = (!visibility).toVisibility()
        binding.noComments.visibility = visibility.toVisibility()
    }

    override fun retryCallback() {
        recyclerViewPaginator.reload()
    }

    override fun onRetryViewClicked(comment: Comment) {
    }

    override fun onReplyButtonClicked(comment: Comment) {
        viewModel.inputs.onReplyClicked(comment, true)
    }

    override fun onFlagButtonClicked(comment: Comment) {
    }

    override fun onCommentGuideLinesClicked(comment: Comment) {
        viewModel.inputs.onShowGuideLinesLinkClicked()
    }

    override fun onCommentPostedSuccessFully(comment: Comment, position: Int) {
        viewModel.inputs.refreshComment(comment, position)
    }

    override fun onCommentPostedFailed(comment: Comment, position: Int) {
        viewModel.inputs.refreshCommentCardInCaseFailedPosted(comment, position)
    }

    override fun onShowCommentClicked(comment: Comment) {
        viewModel.inputs.onShowCanceledPledgeComment(comment)
    }

    override fun onCommentRepliesClicked(comment: Comment) {
        viewModel.inputs.onReplyClicked(comment, false)
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
    private fun startThreadActivity(commentData: CommentCardData, openKeyboard: Boolean) {
        val threadIntent = Intent(this, ThreadActivity::class.java).apply {
            putExtra(IntentKey.COMMENT_CARD_DATA, commentData)
            putExtra(IntentKey.REPLY_EXPAND, openKeyboard)
        }

        startActivity(threadIntent)
        this.let {
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }

    private fun startThreadActivityFromDeepLink(commentData: CommentCardData) {
        val threadIntent = Intent(this, ThreadActivity::class.java).apply {
            putExtra(IntentKey.COMMENT_CARD_DATA, commentData)
            putExtra(IntentKey.REPLY_EXPAND, false)
            putExtra(IntentKey.REPLY_SCROLL_BOTTOM, true)
        }

        startActivity(threadIntent)
        this.let {
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.commentsRecyclerView.adapter = null
        this.viewModel = null
    }

    companion object {
        const val COMMENT_KICKSTARTER_GUIDELINES = "help/community"
    }
}
