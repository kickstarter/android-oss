package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.ui.adapters.ProjectListAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(DiscoveryPresenter.class)
public class DiscoveryActivity extends BaseActivity<DiscoveryPresenter> {
  ProjectListAdapter adapter;
  @InjectView(R.id.category_spinner) Spinner spinner;
  @InjectView(R.id.recyclerView) RecyclerView recyclerView;
  @InjectView(R.id.toolbar) Toolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("Activity onCreate");

    setContentView(R.layout.discovery_layout);

    // Injection
    ButterKnife.inject(this);
    ((KsrApplication) getApplication()).component().inject(this);

    createToolbar();

    // Setup recycler view
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Timber.d("Activity onDestroy");
  }

  protected void createToolbar() {
    // Simple_spinner_item and simple_spinner_dropdown_item are provided by the
    // platform, they can be replaced when we want to customize the spinner's appearance.
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
      R.array.categories_array,
      android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    // onItemSelected will fire immediately with the default selection
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
        String item = spinner.getItemAtPosition(position).toString();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });
  }

  public void onItemsNext(List<Project> projects) {
    adapter = new ProjectListAdapter(projects, presenter);
    recyclerView.setAdapter(adapter);
  }
}
