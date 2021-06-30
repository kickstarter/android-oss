package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.databinding.ActivityThreadLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.Comment
import com.kickstarter.ui.adapters.RepliesAdapter
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.ThreadViewModel
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

@RequiresActivityViewModel(ThreadViewModel.ViewModel::class)
class ThreadActivity :
    BaseActivity<ThreadViewModel.ViewModel>(),
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

        binding.commentRepliesRecyclerView.adapter = adapter
        binding.commentRepliesRecyclerView.layoutManager = linearLayoutManager

        this.viewModel.getRootComment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { comment ->
                adapter.updateRootCommentCell(comment)
            }

        this.viewModel
            .onCommentReplies()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.adapter.takeData(it.first.reversed(), it.second)
            }

        viewModel.outputs.isFetchingReplies()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                // binding.commentsLoadingIndicator.isVisible = it
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

        viewModel.outputs.showReplyComposer()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.isVisible = it
            }

        binding.replyComposer.setCommentComposerActionClickListener(object :
                OnCommentComposerViewClickedListener {
                override fun onClickActionListener(string: String) {
                    // TODO add Post Replay
                    hideKeyboard()
                }
            })
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
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
        TODO("Not yet implemented")
    }

    override fun loadMoreCallback() {
        recyclerViewPaginator.reload()
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.commentRepliesRecyclerView.adapter = null
        this.viewModel = null
    }
}
