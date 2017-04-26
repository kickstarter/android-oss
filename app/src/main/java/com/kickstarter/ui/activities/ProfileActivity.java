package com.kickstarter.ui.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.ProfileAdapter;
import com.kickstarter.ui.views.IconButton;
import com.kickstarter.viewmodels.ProfileViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(ProfileViewModel.ViewModel.class)
public final class ProfileActivity extends BaseActivity<ProfileViewModel.ViewModel> {
  private ProfileAdapter adapter;
  private RecyclerViewPaginator paginator;

  protected @Bind(R.id.avatar_image_view) ImageView avatarImageView;
  protected @Bind(R.id.backed_count_text_view) TextView backedCountTextView;
  protected @Bind(R.id.backed_text_view) TextView backedTextView;
  protected @Bind(R.id.created_count_text_view) TextView createdCountTextView;
  protected @Bind(R.id.created_text_view) TextView createdTextView;
  protected @Bind(R.id.divider_view) View dividerView;
  protected @Bind(R.id.messages_button) IconButton messagesButton;
  protected @Bind(R.id.recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.user_name_text_view) TextView userNameTextView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_layout);
    ButterKnife.bind(this);

    this.adapter = new ProfileAdapter(viewModel);
    final int spanCount = ViewUtils.isLandscape(this) ? 3 : 2;
    this.recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    this.recyclerView.setAdapter(this.adapter);

    this.paginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage);

    this.viewModel.outputs.avatarImageViewUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(url -> Picasso.with(this).load(url).transform(new CircleTransformation()).into(this.avatarImageView));

    this.viewModel.outputs.backedCountTextViewHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backedCountTextView));

    this.viewModel.outputs.backedCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.backedCountTextView::setText);

    this.viewModel.outputs.backedTextViewHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backedTextView));

    this.viewModel.outputs.createdCountTextViewHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.createdCountTextView));

    this.viewModel.outputs.createdCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.createdCountTextView::setText);

    this.viewModel.outputs.createdTextViewHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.createdTextView));

    this.viewModel.outputs.dividerViewHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.dividerView));

    this.viewModel.outputs.messagesButtonHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.messagesButton));

    this.viewModel.outputs.projects()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::loadProjects);

    this.viewModel.outputs.resumeDiscoveryActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> resumeDiscoveryActivity());

    this.viewModel.outputs.startMessageThreadsActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.startMessageThreadsActivity());

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectActivity);

    this.viewModel.outputs.userNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.userNameTextView::setText);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.paginator.stop();
    this.recyclerView.setAdapter(null);
  }

  @OnClick(R.id.messages_button)
  public void messagesButtonClicked() {
    this.viewModel.inputs.messsagesButtonClicked();
  }

  private void loadProjects(final @NonNull List<Project> projects) {
    if (projects.size() == 0) {
      this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
      this.recyclerView.setPadding(
        0, this.recyclerView.getPaddingTop(), this.recyclerView.getPaddingRight(), this.recyclerView.getPaddingBottom()
      );

      if (ViewUtils.isPortrait(this)) {
        disableNestedScrolling();
      }
    }

    this.adapter.takeProjects(projects);
  }

  @TargetApi(21)
  private void disableNestedScrolling() {
    if (ApiCapabilities.canSetNestingScrollingEnabled()) {
      this.recyclerView.setNestedScrollingEnabled(false);
    }
  }

  private void resumeDiscoveryActivity() {
    ApplicationUtils.resumeDiscoveryActivity(this);
  }

  private void startMessageThreadsActivity() {
    final Intent intent = new Intent(this, MessageThreadsActivity.class);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startProjectActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
