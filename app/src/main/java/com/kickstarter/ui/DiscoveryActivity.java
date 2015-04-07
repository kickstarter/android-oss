package com.kickstarter.ui;

import android.app.Activity;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.kickstarter.BaseActivity;
import com.kickstarter.R;
import com.kickstarter.adapters.ProjectAdapter;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;

import java.util.List;

import javax.inject.Inject;

public class DiscoveryActivity extends BaseActivity {
  @Inject LocationManager locationManager;
  private RecyclerView recyclerView;
  private RecyclerView.Adapter adapter;
  private RecyclerView.LayoutManager layoutManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // After the super.onCreate call we are guaranteed our injections are available

    Fresco.initialize(getApplicationContext());
    setContentView(R.layout.activity_discovery);

    recyclerView = (RecyclerView) findViewById(R.id.projects);

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);

    // use a linear layout manager
    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    KickstarterClient client = new KickstarterClient();
    List<Project> projects = client.fetchProjects().toBlocking().last();

    // specify an adapter
    adapter = new ProjectAdapter(projects);
    recyclerView.setAdapter(adapter);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
