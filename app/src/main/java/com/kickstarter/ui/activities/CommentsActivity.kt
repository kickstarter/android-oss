package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import com.kickstarter.R
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.Comment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.CommentInitialErrorAdapter
import com.kickstarter.ui.adapters.CommentPaginationErrorAdapter
import com.kickstarter.ui.adapters.CommentsAdapter
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.CommentsViewModel.CommentsViewModel
import com.kickstarter.viewmodels.CommentsViewModel.Factory
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class CommentsActivity :
    AppCompatActivity(),
    CommentsAdapter.Delegate,
    CommentPaginationErrorAdapter.Delegate {
    private lateinit var binding: ActivityCommentsLayoutBinding
    /** comments list and initial load error adapter **/
    private val commentsAdapter = CommentsAdapter(this)
    private val commentPaginationErrorAdapter = CommentPaginationErrorAdapter(this)
    private val commentInitialErrorAdapter = CommentInitialErrorAdapter()

    private lateinit var recyclerViewPaginator: RecyclerViewPaginatorV2

    private lateinit var viewModelFactory: Factory
    private val viewModel: CommentsViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        val view: View = binding.root

        setContentView(view)

        setUpConnectivityStatusCheck(lifecycle)

        val env = this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, intent = intent)
            env
        }

        /** use ConcatAdapter to bind adapters to recycler view and replace the section issue **/
        binding.commentsRecyclerView.adapter =
            ConcatAdapter(commentInitialErrorAdapter, commentsAdapter, commentPaginationErrorAdapter)

        binding.backButton.setOnClickListener {
            this.viewModel.backPressed()
        }

        setupPagination()

        viewModel.outputs.commentsList()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                    comments ->
                commentsAdapter.takeData(comments)
            }
            .addToDisposable(disposables)

        viewModel.outputs.currentUserAvatar()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.commentComposer.setAvatarUrl(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.commentComposerStatus()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.commentComposer.setCommentComposerStatus(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.showCommentComposer()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.commentComposer.isVisible = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.setEmptyState()
            .compose(Transformers.observeForUIV2())
            .subscribe { setEmptyState(it) }
            .addToDisposable(disposables)

        viewModel.outputs.shouldShowInitialLoadErrorUI()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                setEmptyState(false)
                commentInitialErrorAdapter.insertPageError(it)
            }
            .addToDisposable(disposables)

        /*
         * A little delay after new item is inserted
         * This is necessary for the scroll to take effect
         */
        viewModel.outputs.scrollToTop()
            .filter { it == true }
            .compose(Transformers.observeForUIV2())
            .delay(200, TimeUnit.MILLISECONDS)
            .subscribe {
                binding.commentsRecyclerView.smoothScrollToPosition(0)
            }
            .addToDisposable(disposables)

        viewModel.outputs.closeCommentsPage()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                onBackPressedDispatcher.onBackPressed()
            }
            .addToDisposable(disposables)

        viewModel.outputs.shouldShowPaginationErrorUI()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                commentPaginationErrorAdapter.addErrorPaginationCell(it)
            }
            .addToDisposable(disposables)

        binding.commentComposer.setCommentComposerActionClickListener(object : OnCommentComposerViewClickedListener {
            override fun onClickActionListener(string: String) {
                postComment(string)
                hideKeyboard()
            }
        })

        viewModel.outputs.showCommentGuideLinesLink()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                ApplicationUtils.openUrlExternally(this, UrlUtils.appendPath(env?.webEndpoint() ?: "", COMMENT_KICKSTARTER_GUIDELINES))
            }
            .addToDisposable(disposables)

        viewModel.outputs.hasPendingComments()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                if (it.first) {
                    handleBackAction(it.second)
                } else {
                    executeActions(it.second)
                }
            }
            .addToDisposable(disposables)

        this.onBackPressedDispatcher.addCallback {
            handleBackAction()
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

    private fun executeActions(isBackAction: Boolean) {
        if (!isBackAction) {
            viewModel.inputs.refresh()
        } else {
            finishWithAnimation()
        }
    }

    private fun handleBackAction() {
        if (binding.commentComposer.isCommentComposerEmpty() == true) {
            viewModel.inputs.checkIfThereAnyPendingComments(true)
        } else {
            handleBackAction(true)
        }
    }
    private fun setupPagination() {

        recyclerViewPaginator = RecyclerViewPaginatorV2(binding.commentsRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingComments())

        binding.commentsSwipeRefreshLayout.setOnRefreshListener {
            viewModel.inputs.checkIfThereAnyPendingComments(false)
            viewModel.inputs.refresh()
        }

        viewModel.outputs.isFetchingComments()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding.commentsSwipeRefreshLayout.isRefreshing = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.startThreadActivity()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                startThreadActivity(it.first.first, it.first.second, it.second)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startThreadActivityFromDeepLink()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                startThreadActivityFromDeepLink(it.first, it.second)
            }
            .addToDisposable(disposables)
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
    private fun startThreadActivity(commentData: CommentCardData, openKeyboard: Boolean, projectUpdateId: String? = null) {
        val reducedProject = commentData.project?.reduceProjectPayload()
        val threadIntent = Intent(this, ThreadActivity::class.java).apply {
            putExtra(IntentKey.COMMENT_CARD_DATA, commentData.toBuilder().project(reducedProject).build())
            putExtra(IntentKey.REPLY_EXPAND, openKeyboard)
            putExtra(IntentKey.UPDATE_POST_ID, projectUpdateId)
        }

        startActivity(threadIntent)
        this.let {
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }

    private fun startThreadActivityFromDeepLink(commentData: CommentCardData, projectUpdateId: String? = null) {
        val reducedProject = commentData.project?.reduceProjectPayload()
        val threadIntent = Intent(this, ThreadActivity::class.java).apply {
            putExtra(IntentKey.COMMENT_CARD_DATA, commentData.toBuilder().project(reducedProject).build())
            putExtra(IntentKey.REPLY_EXPAND, false)
            putExtra(IntentKey.REPLY_SCROLL_BOTTOM, true)
            putExtra(IntentKey.UPDATE_POST_ID, projectUpdateId)
        }

        startActivity(threadIntent)
        this.let {
            TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.commentsRecyclerView.adapter = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    companion object {
        const val COMMENT_KICKSTARTER_GUIDELINES = "help/community"
    }
}
