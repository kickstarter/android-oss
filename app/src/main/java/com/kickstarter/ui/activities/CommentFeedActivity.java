package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;
import com.kickstarter.viewmodels.CommentFeedViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.*;

@RequiresViewModel(CommentFeedViewModel.class)
public final class CommentFeedActivity extends BaseActivity<CommentFeedViewModel> implements CommentFeedAdapter.Delegate {
  private CommentFeedAdapter adapter;
  private RecyclerViewPaginator recyclerViewPaginator;
  private SwipeRefresher swipeRefresher;
  @Nullable private AlertDialog commentDialog;

  public @Bind(R.id.comment_button) TextView commentButtonTextView;
  protected @Bind(R.id.comment_feed_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
  public @Bind(R.id.comment_feed_recycler_view) RecyclerView recyclerView;
  @Nullable @Bind(R.id.comment_body) EditText commentBodyEditText;
  public @Nullable @Bind(R.id.post_button) TextView postCommentButton;

  @BindString(R.string.social_error_could_not_post_try_again) String postCommentErrorString;
  @BindString(R.string.project_comments_posted) String commentPostedString;

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

    toastMessages()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.showToast(this));

    viewModel.outputs.showCommentButton()
      .map(show -> show ? View.VISIBLE : View.GONE)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(commentButtonTextView::setVisibility);

    viewModel.outputs.showCommentDialog()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showCommentDialog);

    viewModel.outputs.commentPosted()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> dismissCommentDialog());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    recyclerViewPaginator.stop();
    recyclerView.setAdapter(null);
  }

  public void show(final @NonNull Project project, final @NonNull List<Comment> comments,
    final @Nullable User user) {
    adapter.takeProjectComments(project, comments, user);
  }

  @Nullable
  @OnClick({R.id.project_context_view})
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

  public void showCommentDialog(final @NonNull Project project) {
    commentDialog = new AlertDialog.Builder(this)
      .setView(R.layout.comment_dialog)
      .create();
    commentDialog.show();
    commentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    /* Toolbar actions */
    final TextView projectNameTextView = ButterKnife.findById(commentDialog, R.id.comment_project_name);
    final TextView cancelButtonTextView = ButterKnife.findById(commentDialog, R.id.cancel_button);
    commentBodyEditText = ButterKnife.findById(commentDialog, R.id.comment_body);
    postCommentButton = ButterKnife.findById(commentDialog, R.id.post_button);

    projectNameTextView.setText(project.name());
    cancelButtonTextView.setOnClickListener((final @NonNull View v) -> dismissCommentDialog());
    if (commentBodyEditText != null) {
      commentBodyEditText.addTextChangedListener(new TextWatcher() {
        public void beforeTextChanged(final @NonNull CharSequence s, final int start, final int count, final int after) {}
        public void onTextChanged(final @NonNull CharSequence s, final int start, final int before, final int count) {}
        public void afterTextChanged(final @NonNull Editable s) {
          viewModel.inputs.commentBody(s.toString());
        }
      });
    }

    if (postCommentButton != null && commentBodyEditText != null) {
      postCommentButton.setOnClickListener((final @NonNull View v) -> {
        viewModel.postClick(commentBodyEditText.getText().toString());
      });
    }
  }

  public void dismissCommentDialog() {
    if (commentDialog != null) {
      commentDialog.dismiss();
      commentDialog = null;
    }
  }

  public void enablePostButton(final boolean enabled) {
    if (postCommentButton != null) {
      postCommentButton.setEnabled(enabled);
    }
  }

  public void disablePostButton(final boolean disabled) {
    if (postCommentButton != null) {
      postCommentButton.setEnabled(!disabled);
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
    viewModel.takeLoginSuccess();
  }

  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  private Observable<String> toastMessages() {
    return viewModel.errors.postCommentError()
      .map(ObjectUtils.coalesceWith(postCommentErrorString))
      .mergeWith(viewModel.outputs.commentPosted().map(__ -> commentPostedString));
  }
}
