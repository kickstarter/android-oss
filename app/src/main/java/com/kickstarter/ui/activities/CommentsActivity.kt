package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.loadmore.PaginationHandler
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.models.Comment
import com.kickstarter.ui.adapters.CommentsAdapter
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.CommentsViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CommentsViewModel.ViewModelOutput::class)
class CommentsActivity :
    BaseActivity<CommentsViewModel.ViewModelOutput>(),
    CommentsAdapter.Delegate {
    private lateinit var binding: ActivityCommentsLayoutBinding
    private val adapter = CommentsAdapter(this, object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return threadsAreTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return threadsAreTheSame(oldItem, newItem)
        }

        private fun threadsAreTheSame(oldItem: Any, newItem: Any): Boolean {
            val oldThread = oldItem as Comment
            val newThread = newItem as Comment
            return oldThread.id() == newThread.id()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsLayoutBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        binding.commentsRecyclerView.adapter = adapter

       val loadMoreListView = PaginationHandler(
                adapter,
                binding.commentsRecyclerView,
                binding.commentsSwipeRefreshLayout
        )

        loadMoreListView.onRefreshListener = {
            viewModel.inputs.refresh()
        }

        loadMoreListView.onLoadMoreListener = {
                viewModel.inputs.nextPage()
        }

        viewModel.outputs.enablePagination()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadMoreListView.loadMoreEnabled = it
                }

        viewModel.outputs.isLoadingMoreItems()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadMoreListView.isLoading(it)
                }

        viewModel.outputs.isRefreshing()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadMoreListView.refreshing(it)
                }

        viewModel.outputs.commentsList()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                it?.let { comments -> adapter.takeData(comments) }
            }

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
        val d = visibility
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
    }

    override fun onFlagButtonClicked(comment: Comment) {
    }

    override fun onCommentGuideLinesClicked(comment: Comment) {
    }

    override fun onDestroy() {
        super.onDestroy()
        // recyclerViewPaginator.stop()
        binding.commentsRecyclerView.adapter = null
    }
}
