package com.kickstarter.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;
import com.kickstarter.viewmodels.CommentFeedViewModel;
import com.trello.rxlifecycle.ActivityEvent;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresViewModel(CommentFeedViewModel.class)
public final class CommentFeedActivity extends BaseActivity<CommentFeedViewModel> implements CommentFeedAdapter.Delegate {
  private CommentFeedAdapter adapter;
  private RecyclerViewPaginator recyclerViewPaginator;
  private SwipeRefresher swipeRefresher;

  private @NonNull PublishSubject<AlertDialog> alertDialog = PublishSubject.create();

  protected @Bind(R.id.comment_button) TextView commentButtonTextView;
  protected @Bind(R.id.comment_feed_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
  protected @Bind(R.id.comment_feed_recycler_view) RecyclerView recyclerView;

  protected @BindString(R.string.social_error_could_not_post_try_again) String postCommentErrorString;
  protected @BindString(R.string.project_comments_posted) String commentPostedString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.comment_feed_layout);
    ButterKnife.bind(this);

    adapter = new CommentFeedAdapter(this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    recyclerViewPaginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);
    swipeRefresher = new SwipeRefresher(this, swipeRefreshLayout, viewModel.inputs::refresh, viewModel.outputs::isFetchingComments);

    final Observable<TextView> commentBodyEditText = alertDialog
      .map(a -> ButterKnife.findById(a, R.id.comment_body));

    final Observable<TextView> postCommentButton = alertDialog
      .map(a -> ButterKnife.findById(a, R.id.post_button));

    final Observable<TextView> cancelButton = alertDialog
      .map(a -> ButterKnife.findById(a, R.id.cancel_button));

    cancelButton
      .switchMap(RxView::clicks)
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.commentDialogDismissed());

    postCommentButton
      .switchMap(RxView::clicks)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.postCommentClicked());

    commentBodyEditText
      .switchMap(RxTextView::textChanges)
      .map(CharSequence::toString)
      .compose(bindToLifecycle())
      .subscribe(viewModel.inputs::commentBody);

    viewModel.outputs.initialCommentBody()
      .take(1)
      .compose(Transformers.combineLatestPair(commentBodyEditText))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(ce -> ce.second.append(ce.first));

    viewModel.outputs.commentFeedData()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::takeData);

    commentBodyEditText
      .compose(Transformers.takeWhen(viewModel.outputs.commentIsPosted()))
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::clearTextView);

    viewModel.outputs.postButtonIsEnabled()
      .compose(Transformers.combineLatestPair(postCommentButton))
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(bb -> setPostButtonEnabled(bb.second, bb.first));

    viewModel.outputs.showCommentButton()
      .map(show -> show ? View.VISIBLE : View.GONE)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(commentButtonTextView::setVisibility);

    viewModel.outputs.showCommentDialog()
      .filter(projectAndShow -> projectAndShow != null)
      .map(projectAndShow -> projectAndShow.first)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showCommentDialog);

    alertDialog
      .compose(Transformers.takeWhen(viewModel.outputs.dismissCommentDialog()))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(this::dismissCommentDialog);

    lifecycle()
      .compose(Transformers.combineLatestPair(alertDialog))
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

    recyclerViewPaginator.stop();
    recyclerView.setAdapter(null);
  }

  @Nullable
  @OnClick(R.id.project_context_view)
  public void projectContextViewClick() {
    back();
  }

  public void commentFeedLogin() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.COMMENT_FEED);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
  }

  @OnClick(R.id.comment_button)
  protected void commentButtonClicked() {
    viewModel.inputs.commentButtonClicked();
  }

  public void clearTextView(final @NonNull TextView textView) {
    textView.setText("");
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
      viewModel.inputs.commentDialogDismissed();
    });

    alertDialog.onNext(commentDialog);
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
  public void emptyCommentFeedLoginClicked(final @NonNull EmptyCommentFeedViewHolder viewHolder) {
    commentFeedLogin();
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

  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  private Observable<String> toastMessages() {
    return viewModel.errors.postCommentError()
      .map(ObjectUtils.coalesceWith(postCommentErrorString))
      .mergeWith(viewModel.outputs.commentIsPosted().map(__ -> commentPostedString));
  }
}
