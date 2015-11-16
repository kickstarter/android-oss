package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Paginator;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(CommentFeedPresenter.class)
public final class CommentFeedActivity extends BaseActivity<CommentFeedPresenter> implements CommentFeedAdapter.Delegate {
  private CommentFeedAdapter adapter;
  private Project project;
  private Paginator paginator;
  @Nullable private AlertDialog commentDialog;

  public @Bind(R.id.comment_button) TextView commentButtonTextView;
  public @Bind(R.id.comment_feed_recycler_view) RecyclerView recyclerView;
  @Nullable @Bind(R.id.comment_body) EditText commentBodyEditText;
  public @Nullable @Bind(R.id.post_button) TextView postCommentButton;

  @BindString(R.string.Post_comment_error) String postCommentErrorString;
  @BindString(R.string.Comment_posted) String commentPostedString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.comment_feed_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    project = intent.getParcelableExtra(getString(R.string.intent_project));

    adapter = new CommentFeedAdapter(this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    presenter.inputs.initialProject(project);

    paginator = new Paginator(recyclerView, presenter.inputs::nextPage);

    addSubscription(toastMessages()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::displayToast)
    );

    addSubscription(presenter.outputs.showCommentButton()
        .map(show -> show ? View.VISIBLE : View.GONE)
        .subscribe(commentButtonTextView::setVisibility)
    );

    addSubscription(presenter.outputs.showCommentDialog()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> showCommentDialog())
    );

    addSubscription(presenter.outputs.commentPosted()
        .compose(Transformers.ignoreValues())
        .subscribe(__ -> dismissCommentDialog())
    );
  }

  public void show(@NonNull final Project project, @NonNull final List<Comment> comments,
    @Nullable final User user) {
    adapter.takeProjectComments(project, comments, user);
  }

  @Nullable
  @OnClick({R.id.project_context_view})
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void commentFeedLogin() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true)
      .putExtra(getString(R.string.intent_login_type), LoginToutActivity.REASON_GENERIC);
    startActivityForResult(intent, ActivityRequestCodes.COMMENT_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  @OnClick(R.id.comment_button)
  public void showCommentDialog() {
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
    cancelButtonTextView.setOnClickListener((@NonNull final View v) -> dismissCommentDialog());
    if (commentBodyEditText != null) {
      commentBodyEditText.addTextChangedListener(new TextWatcher() {
        public void beforeTextChanged(@NonNull final CharSequence s, final int start, final int count, final int after) {}
        public void onTextChanged(@NonNull final CharSequence s, final int start, final int before, final int count) {}
        public void afterTextChanged(@NonNull final Editable s) {
          presenter.inputs.commentBody(s.toString());
        }
      });
    }

    if (postCommentButton != null && commentBodyEditText != null) {
      postCommentButton.setOnClickListener((@NonNull final View v) -> {
        presenter.postClick(commentBodyEditText.getText().toString());
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
  public void projectContextClicked(@NonNull final ProjectContextViewHolder viewHolder) {
    onBackPressed();
  }

  @Override
  public void emptyCommentFeedLoginClicked(@NonNull final EmptyCommentFeedViewHolder viewHolder) {
    commentFeedLogin();
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.COMMENT_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }
    if (resultCode != RESULT_OK) {
      return;
    }
    presenter.takeLoginSuccess();
  }

  private Observable<String> toastMessages() {
    return presenter.errors.postCommentError()
      .map(ObjectUtils.coalesceWith(postCommentErrorString))
      .mergeWith(presenter.outputs.commentPosted().map(__ -> commentPostedString));
  }
}
