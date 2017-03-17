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
import com.kickstarter.viewmodels.ProfileViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresActivityViewModel(ProfileViewModel.ViewModel.class)
public final class ProfileActivity extends BaseActivity<ProfileViewModel.ViewModel> {
  private ProfileAdapter adapter;
  private RecyclerViewPaginator paginator;

  protected @Bind(R.id.avatar_image_view) ImageView avatarImageView;
  protected @Bind(R.id.user_name_text_view) TextView userNameTextView;
  protected @Bind(R.id.created_count_text_view) TextView createdCountTextView;
  protected @Bind(R.id.backed_count_text_view) TextView backedCountTextView;
  protected @Bind(R.id.created_text_view) TextView createdTextView;
  protected @Bind(R.id.backed_text_view) TextView backedTextView;
  protected @Bind(R.id.divider_view) View dividerView;
  protected @Bind(R.id.recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_layout);
    ButterKnife.bind(this);

    adapter = new ProfileAdapter(viewModel);
    final int spanCount = ViewUtils.isLandscape(this) ? 3 : 2;
    recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    recyclerView.setAdapter(adapter);

    paginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);

    viewModel.outputs.avatarImageViewUrl()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(url -> Picasso.with(this).load(url).transform(new CircleTransformation()).into(avatarImageView));

    viewModel.outputs.backedCountTextViewHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(this.backedCountTextView));

    viewModel.outputs.backedCountTextViewText()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.backedCountTextView::setText);

    viewModel.outputs.backedTextViewHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(this.backedTextView));

    viewModel.outputs.createdCountTextViewHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(this.createdCountTextView));

    viewModel.outputs.createdCountTextViewText()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.createdCountTextView::setText);

    viewModel.outputs.createdTextViewHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(this.createdTextView));

    viewModel.outputs.dividerViewHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(this.dividerView));

    viewModel.outputs.projects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadProjects);

    viewModel.outputs.resumeDiscoveryActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> resumeDiscoveryActivity());

    viewModel.outputs.startMessageThreadsActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.startMessageThreadsActivity());

    viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startProjectActivity);

    viewModel.outputs.userNameTextViewText()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.userNameTextView::setText);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    paginator.stop();
    recyclerView.setAdapter(null);
  }

  @OnClick(R.id.messages_button)
  public void messagesButtonClicked() {
    viewModel.inputs.messsagesButtonClicked();
  }

  private void loadProjects(final @NonNull List<Project> projects) {
    if (projects.size() == 0) {
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      recyclerView.setPadding(0, recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());
      if (ViewUtils.isPortrait(this)) {
        disableNestedScrolling();
      }
    }

    adapter.takeProjects(projects);
  }

  @TargetApi(21)
  private void disableNestedScrolling() {
    if (ApiCapabilities.canSetNestingScrollingEnabled()) {
      recyclerView.setNestedScrollingEnabled(false);
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
