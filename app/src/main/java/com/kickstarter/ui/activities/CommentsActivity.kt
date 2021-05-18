package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.CommentsViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CommentsViewModel.ViewModel::class)
class CommentsActivity : BaseActivity<CommentsViewModel.ViewModel>() {
    private lateinit var binding: ActivityCommentsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsLayoutBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

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

        binding.commentComposer.setCommentComposerActionClickListener(object : OnCommentComposerViewClickedListener {
            override fun onClickActionListener(string: String) {
            }
        })
    }
}
