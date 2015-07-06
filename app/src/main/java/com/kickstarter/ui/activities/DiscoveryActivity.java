package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.ApiResponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.ProjectListAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(DiscoveryPresenter.class)
public class DiscoveryActivity extends BaseActivity<DiscoveryPresenter> {
  ProjectListAdapter adapter;
  @InjectView(R.id.recyclerView) RecyclerView recyclerView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.discovery_layout);
    ButterKnife.inject(this);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  public void onItemsNext(final List<Project> projects) {
    Timber.d("onItemsNext %s", this.toString());
    adapter = new ProjectListAdapter(projects, presenter);
    recyclerView.setAdapter(adapter);
  }

  public void showBuildAlert(final InternalBuildEnvelope envelope) {
    new AlertDialog.Builder(this)
      .setTitle("Upgrade app")
      .setMessage("A newer build is available. Download upgrade?")
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        Intent intent = new Intent(this, DownloadBetaActivity.class)
          .putExtra("internalBuildEnvelope", envelope);
        startActivity(intent);
      })
      .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
      })
      .setIcon(android.R.drawable.ic_dialog_alert)
      .show();
  }
}
