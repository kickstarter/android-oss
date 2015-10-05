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
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapters.CommentFeedAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(CommentFeedPresenter.class)
public class CommentFeedActivity extends BaseActivity<CommentFeedPresenter> {
  private CommentFeedAdapter adapter;
  private Project project;

  @Bind(R.id.comment_button) TextView commentButtonTextView;
  @Bind(R.id.comment_feed_recycler_view) RecyclerView recyclerView;
  @Nullable @Bind(R.id.context_photo) ImageView projectPhotoImageView;
  @Nullable @Bind(R.id.project_name) TextView projectNameTextView;
  @Nullable @Bind(R.id.creator_name) TextView creatorNameTextView;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.comment_feed_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    project = intent.getParcelableExtra(getString(R.string.intent_project));
    presenter.initialize(project);

    adapter = new CommentFeedAdapter(presenter);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  public void show(@NonNull final Project project, @NonNull final List<Comment> comments,
    @Nullable final User user) {
    if (project.isBacking()) {
      commentButtonTextView.setVisibility(View.VISIBLE);
    } else {
      commentButtonTextView.setVisibility(View.GONE);
    }
    adapter.takeProjectComments(project, comments, user);
  }

  @Nullable
  @OnClick({R.id.nav_back_button, R.id.project_context_view})
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void commentFeedLogin() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent, ActivityRequestCodes.COMMENT_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  @OnClick(R.id.comment_button)
  public void showCommentDialog() {
    // todo: grab project from presenter rather than activity to have latest project
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setView(getLayoutInflater().inflate(R.layout.comment_dialog, null));

    final AlertDialog dialog = builder.create();
    dialog.show();
    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    /* Toolbar actions */
    final TextView projectNameTextView = ButterKnife.findById(dialog, R.id.comment_project_name);
    final TextView cancelButtonTextView = ButterKnife.findById(dialog, R.id.cancel_button);
    final EditText commentBodyEditText = ButterKnife.findById(dialog, R.id.comment_body);
    final TextView postCommentTextView = ButterKnife.findById(dialog, R.id.post_button);

    projectNameTextView.setText(project.name());
    cancelButtonTextView.setOnClickListener((final View v) -> dialog.dismiss());
    postCommentTextView.setEnabled(false);

    commentBodyEditText.addTextChangedListener(
      // Set a text watcher to check for null comment body
      new TextWatcher() {
        @Override
        public void beforeTextChanged(@NonNull final CharSequence charSequence, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(@NonNull final CharSequence charSequence, int start, int before, int count) {
          togglePostTextView(postCommentTextView, charSequence);
        }
        @Override
        public void afterTextChanged(final Editable editable) {
        }
      }
    );

    postCommentTextView.setOnClickListener((final View v) -> {
      presenter.postCommentOnClick(project, commentBodyEditText.getText().toString());
      dialog.dismiss();
    });
  }

  public void togglePostTextView(@NonNull final TextView textView, @NonNull final CharSequence charSequence) {
    if (charSequence.length() > 0) {
      textView.setEnabled(true);
    } else {
      textView.setEnabled(false);
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    if (requestCode != ActivityRequestCodes.COMMENT_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }
    if (resultCode != RESULT_OK) {
      return;
    }
    presenter.takeLoginSuccess();
  }
}
