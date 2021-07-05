package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.SwipeRefresher
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.Comment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.CommentsAdapter
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.CommentsViewModel
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

@RequiresActivityViewModel(CommentsViewModel.ViewModel::class)
class CommentsActivity :
    BaseActivity<CommentsViewModel.ViewModel>(),
    CommentsAdapter.Delegate {
    private lateinit var binding: ActivityCommentsLayoutBinding
    private val adapter = CommentsAdapter(this)

    private lateinit var recyclerViewPaginator: RecyclerViewPaginator
    private lateinit var swipeRefresher: SwipeRefresher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsLayoutBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        binding.commentsRecyclerView.adapter = adapter

        binding.backButton.setOnClickListener { viewModel.inputs.backPressed() }

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

        viewModel.outputs.initialLoadCommentsError()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setEmptyState(false)
                adapter.insertPageError()
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
                adapter.addErrorPaginationCell(it)
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
    }

    private fun closeCommentsActivity() {
        super.back()
        this.finishActivity(taskId)
    }

    private fun setupPagination() {

        recyclerViewPaginator = RecyclerViewPaginator(binding.commentsRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingComments())

        swipeRefresher = SwipeRefresher(
            this, binding.commentsSwipeRefreshLayout, { viewModel.inputs.refresh() }
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

    override fun emptyCommentsLoginClicked(viewHolder: EmptyCommentsViewHolder?) {
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

    override fun onCommentPostedSuccessFully(comment: Comment) {
        viewModel.inputs.refreshComment(comment)
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
