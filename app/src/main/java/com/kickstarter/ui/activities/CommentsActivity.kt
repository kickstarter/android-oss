package com.kickstarter.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import com.kickstarter.R
import com.kickstarter.databinding.CommentsLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RecyclerViewPaginator
import com.kickstarter.libs.SwipeRefresher
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.CommentsAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder
import com.kickstarter.ui.viewholders.ProjectContextViewHolder
import com.kickstarter.viewmodels.CommentsViewModel
import com.trello.rxlifecycle.ActivityEvent
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject

@RequiresActivityViewModel(CommentsViewModel.ViewModel::class)
class CommentsActivity : BaseActivity<CommentsViewModel.ViewModel>(), CommentsAdapter.Delegate {
    private val adapter = CommentsAdapter(this)
    private lateinit var recyclerViewPaginator: RecyclerViewPaginator
    private lateinit var swipeRefresher: SwipeRefresher
    private val alertDialog = PublishSubject.create<AlertDialog>()
    private lateinit var binding: CommentsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommentsLayoutBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.commentsRecyclerView.adapter = adapter

        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        recyclerViewPaginator = RecyclerViewPaginator(binding.commentsRecyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingComments)

        swipeRefresher = SwipeRefresher(
            this, binding.commentsSwipeRefreshLayout, { viewModel.inputs.refresh() }
        ) { viewModel.outputs.isFetchingComments }

        val commentBodyEditText = alertDialog
            .map { it?.findViewById<TextView>(R.id.comment_body) }

        val postCommentButton = alertDialog
            .map { it?.findViewById<TextView>(R.id.post_button) }

        val cancelButton = alertDialog
            .map { it?.findViewById<TextView>(R.id.cancel_button) }

        cancelButton
            .switchMap { it?.let { RxView.clicks(it) } }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { viewModel.inputs.commentDialogDismissed() }

        postCommentButton
            .switchMap { it?.let { RxView.clicks(it) } }
            .compose(bindToLifecycle())
            .subscribe { viewModel.inputs.postCommentClicked() }

        commentBodyEditText
            .switchMap { it?.let {view -> RxTextView.textChanges(view).skip(1) } }
            .map { obj: CharSequence -> obj.toString() }
            .compose(bindToLifecycle())
            .subscribe { viewModel.inputs.commentBodyChanged(it) }

        viewModel.outputs.currentCommentBody()
            .compose(Transformers.takePairWhen(commentBodyEditText))
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { it.second?.append(it.first) }

        viewModel.outputs.commentsData()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.takeData(it) }

        viewModel.outputs.enablePostButton()
            .compose(Transformers.combineLatestPair(postCommentButton))
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setPostButtonEnabled(it.second, it.first) }

        viewModel.outputs.commentButtonHidden()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ViewUtils.setGone(binding.commentsToolbar.commentButton))

        viewModel.outputs.showCommentDialog()
            .filter { it != null }
            .map { it.first }
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCommentDialog(it) }

        alertDialog
            .compose(Transformers.takeWhen(viewModel.outputs.dismissCommentDialog()))
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { dismissCommentDialog(it) }

        lifecycle()
            .compose(Transformers.combineLatestPair(alertDialog))
            .filter { it.first == ActivityEvent.DESTROY }
            .map { it.second }
            .observeOn(AndroidSchedulers.mainThread())
            // NB: We dont want to bind to lifecycle because we want the destroy event.
            // .compose(bindToLifecycle())
            .take(1)
            .subscribe { dismissCommentDialog(it) }

        toastMessages()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ViewUtils.showToast(this))

        findViewById<View>(R.id.project_context_view)?.setOnClickListener { projectContextViewClick() }

        findViewById<View>(R.id.comment_button)?.setOnClickListener { commentButtonClicked() }
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator.stop()
        binding.commentsRecyclerView.adapter = null
    }

    private fun projectContextViewClick() = back()

    private fun commentsLogin() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.COMMENT_FEED)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
    }

    private fun commentButtonClicked() = viewModel.inputs.commentButtonClicked()

    private fun dismissCommentDialog(dialog: AlertDialog?) = dialog?.dismiss()

    private fun showCommentDialog(project: Project) {
        val commentDialog = AlertDialog.Builder(this)
            .setView(R.layout.comment_dialog)
            .create()
        commentDialog.show()
        commentDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        /* Toolbar UI actions */
        val projectNameTextView = commentDialog.findViewById<TextView>(R.id.comment_project_name)
        projectNameTextView?.text = project.name()

        // Handle cancel-click region outside of dialog modal.
        commentDialog.setOnCancelListener { dialogInterface: DialogInterface -> viewModel.inputs.commentDialogDismissed() }
        alertDialog.onNext(commentDialog)
    }

    private fun setPostButtonEnabled(postCommentButton: TextView?, enabled: Boolean) = postCommentButton?.let { it.isEnabled = enabled }

    override fun projectContextClicked(viewHolder: ProjectContextViewHolder?) = back()

    override fun emptyCommentsLoginClicked(viewHolder: EmptyCommentsViewHolder?) = commentsLogin()

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode != ActivityRequestCodes.LOGIN_FLOW) {
            return
        }
        if (resultCode != RESULT_OK) {
            return
        }
        viewModel.inputs.loginSuccess()
    }

    override fun exitTransition() = TransitionUtils.slideInFromLeft()

    private fun toastMessages(): Observable<String> {
        return viewModel.outputs.showPostCommentErrorToast()
            .map(ObjectUtils.coalesceWith(getString(R.string.social_error_could_not_post_try_again)))
            .mergeWith(viewModel.outputs.showCommentPostedToast().map { getString(R.string.project_comments_posted) })
    }
}
