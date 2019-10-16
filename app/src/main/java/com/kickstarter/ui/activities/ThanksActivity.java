package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.ThanksAdapter;
import com.kickstarter.viewmodels.ThanksViewModel;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(ThanksViewModel.ViewModel.class)
public final class ThanksActivity extends BaseActivity<ThanksViewModel.ViewModel> {
  private KSString ksString;

  protected @Bind(R.id.thanks_recycler_view) RecyclerView recyclerView;

  protected @BindString(R.string.project_checkout_games_alert_want_the_coolest_games_delivered_to_your_inbox) String gamesAlertMessage;
  protected @BindString(R.string.project_checkout_games_alert_no_thanks) String gamesAlertNo;
  protected @BindString(R.string.project_checkout_games_alert_yes_please) String gamesAlertYes;
  protected @BindString(R.string.general_alert_buttons_ok) String okString;
  protected @BindString(R.string.profile_settings_newsletter_games) String newsletterGamesString;
  protected @BindString(R.string.profile_settings_newsletter_opt_in_message) String optInMessageString;
  protected @BindString(R.string.profile_settings_newsletter_opt_in_title) String optInTitleString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.thanks_layout);
    ButterKnife.bind(this);

    this.ksString = environment().ksString();

    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(RecyclerView.VERTICAL);
    this.recyclerView.setLayoutManager(layoutManager);

    final ThanksAdapter adapter = new ThanksAdapter(this.viewModel.inputs);
    this.recyclerView.setAdapter(adapter);

    this.viewModel.outputs.adapterData()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::takeData);

    this.viewModel.outputs.showConfirmGamesNewsletterDialog()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> showConfirmGamesNewsletterDialog());

    //I'm not sure why we would attempt to show a dialog after a delay but hopefully this helps
    this.viewModel.outputs.showGamesNewsletterDialog()
      .compose(bindToLifecycle())
      .take(1)
      .delay(700L, TimeUnit.MILLISECONDS)
      .compose(observeForUI())
      .subscribe(__ -> {
        if (!isFinishing()) {
          showGamesNewsletterDialog();
        }
      });

    this.viewModel.outputs.showRatingDialog()
      .compose(bindToLifecycle())
      .take(1)
      .delay(700L, TimeUnit.MILLISECONDS)
      .compose(observeForUI())
      .subscribe(__ -> {
        if (!isFinishing()) {
          showRatingDialog();
        }
      });

    this.viewModel.outputs.startDiscoveryActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startDiscoveryActivity);

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectActivity);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerView.setAdapter(null);
  }

  @OnClick(R.id.close_button)
  protected void closeButtonClick() {
    if (environment().nativeCheckoutPreference().get()) {
      finish();
    } else {
      ApplicationUtils.resumeDiscoveryActivity(this);
    }
  }

  private void showConfirmGamesNewsletterDialog() {
    final String optInDialogMessageString = this.ksString.format(
      this.optInMessageString, "newsletter", this.newsletterGamesString
    );

    final AlertDialog.Builder builder = new AlertDialog.Builder(this)
      .setMessage(optInDialogMessageString)
      .setTitle(this.optInTitleString)
      .setPositiveButton(this.okString, (__, ___) -> {});

    builder.show();
  }

  private void showGamesNewsletterDialog() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this)
      .setMessage(this.gamesAlertMessage)
      .setPositiveButton(this.gamesAlertYes, (__, ___) -> {
        this.viewModel.inputs.signupToGamesNewsletterClick();
      })
      .setNegativeButton(this.gamesAlertNo, (__, ___) -> {
        // Nothing to do!
      });

    builder.show();
  }

  private void showRatingDialog() {
    ViewUtils.showRatingDialog(this);
  }

  private void startDiscoveryActivity(final @NonNull DiscoveryParams params) {
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
      .putExtra(IntentKey.DISCOVERY_PARAMS, params);
    startActivity(intent);
  }

  private void startProjectActivity(final @NonNull Pair<Project, RefTag> projectAndRefTag) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, projectAndRefTag.first)
      .putExtra(IntentKey.REF_TAG, projectAndRefTag.second);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
