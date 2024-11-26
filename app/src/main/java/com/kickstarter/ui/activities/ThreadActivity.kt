package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivityThreadLayoutBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.models.Comment
import com.kickstarter.ui.adapters.RepliesAdapter
import com.kickstarter.ui.adapters.RepliesStatusAdapter
import com.kickstarter.ui.adapters.RootCommentAdapter
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ThreadViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class ThreadActivity :
    AppCompatActivity(),
    RepliesStatusAdapter.Delegate,
    RepliesAdapter.Delegate {

    private lateinit var binding: ActivityThreadLayoutBinding
    private lateinit var ksString: KSString

    /** Replies list adapter **/
    private val repliesAdapter = RepliesAdapter(this)

    /**  Replies cell status viewMore or Error  **/
    private val repliesStatusAdapter = RepliesStatusAdapter(this)

    /** Replies Root comment cell adapter **/
    private val rootCommentAdapter = RootCommentAdapter()

    /** reverse Layout to bind the replies from bottom **/
    private val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)

    private lateinit var recyclerViewPaginator: RecyclerViewPaginatorV2

    private lateinit var viewModelFactory: ThreadViewModel.Factory
    private val viewModel: ThreadViewModel.ThreadViewModel by viewModels { viewModelFactory }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThreadLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)
        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = ThreadViewModel.Factory(env)
            ksString = requireNotNull(env.ksString())
            env
        }

        viewModel.intent(intent)

        recyclerViewPaginator = RecyclerViewPaginatorV2(
            binding.commentRepliesRecyclerView,
            { viewModel.inputs.nextPage() },
            viewModel.outputs.isFetchingReplies(),
            false
        )

        /** use ConcatAdapter to bind adapters to recycler view and replace the section issue **/
        binding.commentRepliesRecyclerView.adapter =
            ConcatAdapter(repliesAdapter, repliesStatusAdapter, rootCommentAdapter)
        binding.commentRepliesRecyclerView.layoutManager = linearLayoutManager

        this.viewModel.outputs.getRootComment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { comment ->
                /** bind root comment by updating the adapter list**/
                rootCommentAdapter.updateRootCommentCell(comment)
            }
            .addToDisposable(disposables)

        this.viewModel.outputs
            .onCommentReplies()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                linearLayoutManager.stackFromEnd = false
            }
            .subscribe {
                /** bind View more cell if the replies more than 7 or update after refresh initial error state **/
                this.repliesStatusAdapter.addViewMoreCell(it.second)
                if (it.first.isNotEmpty()) {
                    this.repliesAdapter.takeData(it.first)
                }
            }
            .addToDisposable(disposables)

        viewModel.outputs.shouldShowPaginationErrorUI()
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it }
            .subscribe {
                /** bind Error Pagination cell **/
                repliesStatusAdapter.addErrorPaginationCell(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.initialLoadCommentsError()
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it }
            .subscribe {
                /** bind Error initial loading cell **/
                repliesStatusAdapter.addInitiallyLoadingErrorCell(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.isFetchingReplies()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.repliesLoadingIndicator.isVisible = it
            }
            .addToDisposable(disposables)

        this.viewModel.shouldFocusOnCompose()
            .delay(30, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { shouldOpenKeyboard ->
                binding.replyComposer.requestCommentComposerKeyBoard(shouldOpenKeyboard)
            }
            .addToDisposable(disposables)

        viewModel.outputs.currentUserAvatar()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.setAvatarUrl(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.replyComposerStatus()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.setCommentComposerStatus(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.scrollToBottom()
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.commentRepliesRecyclerView.smoothScrollToPosition(0) }
            .addToDisposable(disposables)

        viewModel.outputs.showReplyComposer()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.isVisible = it
            }
            .addToDisposable(disposables)

        viewModel.outputs.loadMoreReplies()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                recyclerViewPaginator.reload()
            }
            .addToDisposable(disposables)

        binding.replyComposer.setCommentComposerActionClickListener(
            object : OnCommentComposerViewClickedListener {
                override fun onClickActionListener(string: String) {
                    postReply(string)
                    hideKeyboard()
                }
            }
        )

        viewModel.outputs.showCommentGuideLinesLink()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                ApplicationUtils.openUrlExternally(
                    this,
                    UrlUtils.appendPath(
                        environment?.webEndpoint() ?: "",
                        CommentsActivity.COMMENT_KICKSTARTER_GUIDELINES
                    )
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.hasPendingComments()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) handleBackAction() else viewModel.inputs.backPressed()
            }
            .addToDisposable(disposables)

        viewModel.outputs.closeThreadActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                closeCommentsActivity()
            }
            .addToDisposable(disposables)

        onBackPressedDispatcher.addCallback {
            if (binding.replyComposer.isCommentComposerEmpty() == true) {
                viewModel.inputs.checkIfThereAnyPendingComments()
            } else {
                handleBackAction()
            }
        }
    }

    private fun handleBackAction() {
        this.showAlertDialog(
            getString(R.string.Your_comment_wasnt_posted),
            getString(R.string.You_will_lose_the_comment_if_you_leave_this_page),
            getString(R.string.Cancel),
            getString(R.string.Leave_page),
            false,
            positiveAction = {
            },
            negativeAction = {
                viewModel.inputs.backPressed()
            }
        )
    }

    private fun closeCommentsActivity() {
        setResult(taskId)
        finish()
    }

    fun postReply(comment: String) {
        this.viewModel.inputs.insertNewReplyToList(comment, DateTime.now())
        this.binding.replyComposer.clearCommentComposer()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    override fun onRetryViewClicked(comment: Comment) {
    }

    override fun onReplyButtonClicked(comment: Comment) {
    }

    override fun onFlagButtonClicked(comment: Comment) {
    }

    override fun onCommentGuideLinesClicked(comment: Comment) {
        viewModel.inputs.onShowGuideLinesLinkClicked()
    }

    override fun onCommentRepliesClicked(comment: Comment) {
    }

    override fun onCommentPostedFailed(comment: Comment, position: Int) {
        viewModel.inputs.refreshCommentCardInCaseFailedPosted(comment, position)
    }

    override fun onCommentPostedSuccessFully(comment: Comment, position: Int) {
        viewModel.inputs.refreshCommentCardInCaseSuccessPosted(comment, position)
    }

    override fun loadMoreCallback() {
        viewModel.inputs.reloadRepliesPage()
    }

    override fun retryCallback() {
        viewModel.inputs.reloadRepliesPage()
    }

    override fun onShowCommentClicked(comment: Comment) {
        viewModel.inputs.onShowCanceledPledgeComment(comment)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.commentRepliesRecyclerView.adapter = null
    }
}
