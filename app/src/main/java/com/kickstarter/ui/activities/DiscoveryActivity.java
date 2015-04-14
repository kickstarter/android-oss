package com.kickstarter.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.ui.adapters.ProjectListAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DiscoveryActivity extends ActionBarActivity {
  ProjectListAdapter adapter;
  private static DiscoveryPresenter presenter;
  private DiscoverySpinnerAdapter spinnerAdapter = new DiscoverySpinnerAdapter();
  @InjectView(R.id.category_spinner) Spinner spinner;
  @InjectView(R.id.recyclerView) RecyclerView recyclerView;
  @InjectView(R.id.toolbar) Toolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.discovery_layout);

    // Injection
    ButterKnife.inject(this);
    ((KsrApplication) getApplication()).component().inject(this);

    createToolbar();

    // Setup recycler view
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

  protected void createToolbar() {
    setSupportActionBar(toolbar);

    // Simple_spinner_item and simple_spinner_dropdown_item are provided by the
    // platform, they can be replaced when we want to customize the spinner's appearance.
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
      R.array.categories_array,
      android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  public void onItemsNext(List<Project> projects) {
    adapter = new ProjectListAdapter(projects, presenter);
    recyclerView.setAdapter(adapter);
  }
}
