package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.databinding.ActivityThreadLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Comment
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.viewmodels.ThreadViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ThreadViewModel.ViewModel::class)
class ThreadActivity : BaseActivity<ThreadViewModel.ViewModel>() {

    private lateinit var binding: ActivityThreadLayoutBinding
    private lateinit var ksString: KSString

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThreadLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ksString = environment().ksString()

        this.viewModel.getRootComment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { comment ->
                configureRootCommentView(comment)
            }

        this.viewModel.shouldFocusOnCompose()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { shouldOpenKeyboard ->
                // TODO: Once compose view is integrated we can set focus and open the keyboard
            }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    private fun configureRootCommentView(comment: Comment) {
        binding.commentsCardView.setCommentUserName(comment.author().name())
        binding.commentsCardView.setCommentBody(comment.body())
        binding.commentsCardView.hideReplyViewGroup()
        binding.commentsCardView.setCommentPostTime(DateTimeUtils.relative(this, ksString, comment.createdAt()))
        binding.commentsCardView.setCommentUserName(comment.author().name())
        binding.commentsCardView.setAvatarUrl(comment.author().avatar().medium())
    }
}
