package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.BackingWrapper;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.RewardItemsAdapter;
import com.kickstarter.ui.data.ProjectData;
import com.kickstarter.ui.fragments.BackingFragment;
import com.kickstarter.viewmodels.BackingViewModel;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(BackingViewModel.ViewModel.class)
public final class BackingActivity extends BaseActivity<BackingViewModel.ViewModel> {

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.backing_layout);

    this.viewModel.outputs.showBackingFragment()
            .compose(bindToLifecycle())
            .subscribe(this::startBackingFragment);
  }

  private void startBackingFragment(BackingWrapper backingWrapper) {
    ProjectData data = ProjectData.Companion.builder()
            .project(backingWrapper.component3())
            .build();
    backingFragment().configureWith(data);

    getSupportFragmentManager().beginTransaction()
            .show(backingFragment())
            .commit();
  }

  private BackingFragment backingFragment() {
    return  (BackingFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_backing);
  }

}
