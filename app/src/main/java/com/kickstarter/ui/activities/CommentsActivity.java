package com.kickstarter.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.CommentsAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.viewholders.EmptyCommentsViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;
import com.kickstarter.viewmodels.CommentsViewModel;
import com.trello.rxlifecycle.ActivityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(CommentsViewModel.ViewModel.class)
public final class CommentsActivity extends BaseActivity<CommentsViewModel.ViewModel> implements CommentsAdapter.Delegate {
  private CommentsAdapter adapter;
  private RecyclerViewPaginator recyclerViewPaginator;
  private SwipeRefresher swipeRefresher;

  private @NonNull PublishSubject<AlertDialog> alertDialog = PublishSubject.create();

  protected @Bind(R.id.comment_button) TextView commentButtonTextView;
  protected @Bind(R.id.comments_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
  protected @Bind(R.id.comments_recycler_view) RecyclerView recyclerView;

  protected @BindString(R.string.social_error_could_not_post_try_again) String postCommentErrorString;
  protected @BindString(R.string.project_comments_posted) String commentPostedString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.comments_layout);
    ButterKnife.bind(this);

    this.adapter = new CommentsAdapter(this);
    this.recyclerView.setAdapter(this.adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.recyclerViewPaginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage, this.viewModel.outputs.isFetchingComments());
    this.swipeRefresher = new SwipeRefresher(
      this, this.swipeRefreshLayout, this.viewModel.inputs::refresh, this.viewModel.outputs::isFetchingComments
    );

    final Observable<TextView> commentBodyEditText = this.alertDialog
      .map(a -> ButterKnife.findById(a, R.id.comment_body));

    final Observable<TextView> postCommentButton = this.alertDialog
      .map(a -> ButterKnife.findById(a, R.id.post_button));

    final Observable<TextView> cancelButton = this.alertDialog
      .map(a -> ButterKnife.findById(a, R.id.cancel_button));

    cancelButton
      .switchMap(RxView::clicks)
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.commentDialogDismissed());

    postCommentButton
      .switchMap(RxView::clicks)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.postCommentClicked());

    commentBodyEditText
      .switchMap(t -> RxTextView.textChanges(t).skip(1))
      .map(CharSequence::toString)
      .compose(bindToLifecycle())
      .subscribe(this.viewModel.inputs::commentBodyChanged);

    this.viewModel.outputs.currentCommentBody()
      .compose(Transformers.takePairWhen(commentBodyEditText))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(ce -> ce.second.append(ce.first));

    this.viewModel.outputs.commentsData()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.adapter::takeData);

    this.viewModel.outputs.enablePostButton()
      .compose(combineLatestPair(postCommentButton))
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(bb -> setPostButtonEnabled(bb.second, bb.first));

    this.viewModel.outputs.commentButtonHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(this.commentButtonTextView));

    this.viewModel.outputs.showCommentDialog()
      .filter(projectAndShow -> projectAndShow != null)
      .map(projectAndShow -> projectAndShow.first)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showCommentDialog);

    this.alertDialog
      .compose(takeWhen(this.viewModel.outputs.dismissCommentDialog()))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(this::dismissCommentDialog);

    lifecycle()
      .compose(combineLatestPair(this.alertDialog))
      .filter(ad -> ad.first == ActivityEvent.DESTROY)
      .map(ad -> ad.second)
      .observeOn(AndroidSchedulers.mainThread())
      // NB: We dont want to bind to lifecycle because we want the destroy event.
      // .compose(bindToLifecycle())
      .take(1)
      .subscribe(this::dismissCommentDialog);

    toastMessages()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.showToast(this));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    this.recyclerViewPaginator.stop();
    this.recyclerView.setAdapter(null);
  }

  @Nullable
  @OnClick(R.id.project_context_view)
  public void projectContextViewClick() {
    back();
  }

  public void commentsLogin() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.COMMENT_FEED);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
  }

  @OnClick(R.id.comment_button)
  protected void commentButtonClicked() {
    this.viewModel.inputs.commentButtonClicked();
  }

  public void dismissCommentDialog(final @Nullable AlertDialog dialog) {
    if (dialog != null) {
      dialog.dismiss();
    }
  }

  public void showCommentDialog(final @NonNull Project project) {
    final AlertDialog commentDialog = new AlertDialog.Builder(this)
      .setView(R.layout.comment_dialog)
      .create();
    commentDialog.show();
    commentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    /* Toolbar UI actions */
    final TextView projectNameTextView = ButterKnife.findById(commentDialog, R.id.comment_project_name);
    projectNameTextView.setText(project.name());

    // Handle cancel-click region outside of dialog modal.
    commentDialog.setOnCancelListener((final @NonNull DialogInterface dialogInterface) -> {
      this.viewModel.inputs.commentDialogDismissed();
    });

    this.alertDialog.onNext(commentDialog);
  }

  public void setPostButtonEnabled(final @Nullable TextView postCommentButton, final boolean enabled) {
    if (postCommentButton != null) {
      postCommentButton.setEnabled(enabled);
    }
  }

  @Override
  public void projectContextClicked(final @NonNull ProjectContextViewHolder viewHolder) {
    back();
  }

  @Override
  public void emptyCommentsLoginClicked(final @NonNull EmptyCommentsViewHolder viewHolder) {
    commentsLogin();
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);

    if (requestCode != ActivityRequestCodes.LOGIN_FLOW) {
      return;
    }
    if (resultCode != RESULT_OK) {
      return;
    }
    viewModel.inputs.loginSuccess();
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  private Observable<String> toastMessages() {
    return viewModel.outputs.showPostCommentErrorToast()
      .map(ObjectUtils.coalesceWith(this.postCommentErrorString))
      .mergeWith(this.viewModel.outputs.showCommentPostedToast().map(__ -> this.commentPostedString));
  }
}
