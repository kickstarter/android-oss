package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.KickstarterClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
  @InjectView(R.id.email_address) TextView email_address;
  @InjectView(R.id.password) TextView password;
  @InjectView(R.id.login_button) Button login_button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_layout);
    ButterKnife.inject(this);

    login_button.setOnClickListener(v -> {
      Timber.d("login_button clicked");

      KickstarterClient client = new KickstarterClient();
      AccessTokenEnvelope envelope = client.login(email_address.getText().toString(), password.getText().toString()).toBlocking().last();
      User.setCurrent(envelope.user);

      Intent intent = new Intent(this, DiscoveryActivity.class);
      startActivity(intent);
    });

  }
}
