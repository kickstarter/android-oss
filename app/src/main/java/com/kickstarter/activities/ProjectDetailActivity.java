package com.kickstarter.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

public class ProjectDetailActivity extends Activity {

  private Project project;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    this.project = intent.getExtras().getParcelable("project");
    setContentView(R.layout.project_detail_layout);
    TextView textView = (TextView) findViewById(R.id.category);
    textView.setText(project.category().name());
    Uri uri = Uri.parse(project.photo().full());
    Picasso.with(this).load(uri).into((ImageView) findViewById(R.id.photo));
  }
}
