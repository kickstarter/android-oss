package com.kickstarter.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.adapters.ProjectListAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DiscoveryActivity extends Activity {
  RecyclerView recyclerView;
  ProjectListAdapter adapter;
  private static DiscoveryPresenter presenter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Injection
    ButterKnife.inject(this);
    ((KsrApplication) getApplication()).component().inject(this);

    // Setup recycler view
    setContentView(R.layout.discovery_layout);
    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    if (presenter == null) {
      presenter = new DiscoveryPresenter();
    }
    presenter.onTakeView(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    presenter.onTakeView(null);
    if (isFinishing())
      presenter = null;
  }

  public void onItemsNext(List<Project> projects) {
    adapter = new ProjectListAdapter(projects, presenter);
    recyclerView.setAdapter(adapter);
  }
}
