package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.Comment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.CommentsAdapter
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.CommentsViewModel
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

        viewModel.outputs.commentsList()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { it?.let { comments -> adapter.takeData(comments) } }

        viewModel.outputs.currentUserAvatar()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentComposer.setAvatarUrl(it)
            }

        viewModel.outputs.enableCommentComposer()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentComposer.isDisabledViewVisible(!it)
            }

        viewModel.outputs.showCommentComposer()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentComposer.isVisible = true
            }

        viewModel.outputs.setEmptyState()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::setEmptyState)

        binding.commentComposer.setCommentComposerActionClickListener(object : OnCommentComposerViewClickedListener {
            override fun onClickActionListener(string: String) {
            }
        })
    }

    fun setEmptyState(visibility: Boolean) {
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

    /**
     * Start the Thread activity with
     * @param comment the selected comment to reply
     * @param openKeyboard
     *      true: he focus needs to be on the composer view and set the keyboard open when open the activity
     *      false: in case we just need to open the replies screen
     *
     * // TODO: Once the viewReplies UI is completed call this method with openKeyboard = false
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
}
