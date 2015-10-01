package com.kickstarter.ui.adapters;

import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectViewHolder;
import com.kickstarter.ui.viewholders.RewardViewHolder;

import java.util.Collections;

import rx.Observable;

public class ProjectAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectViewHolder.Delegate, RewardViewHolder.Delegate {}

  public ProjectAdapter(@NonNull final Delegate delegate) {
    this.delegate = delegate;
  }

  protected int layout(@NonNull final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_main_layout;
    } else {
      return R.layout.reward_card_view;
    }
  }

  /**
   * Populate adapter data when we know we're working with a Project object.
   */
  public void takeProject(@NonNull final Project project) {
    data().clear();
    data().add(Collections.singletonList(project));

    // todo: customize RxUtils from() to handle null values
    if (project.hasRewards()) {
      data().add(Observable.from(project.rewards())
        .filter(Reward::isReward)
        .map(reward -> Pair.create(project, reward))
        .toList().toBlocking().single()
      );
    }
    notifyDataSetChanged();
  }

  protected KsrViewHolder viewHolder(final int layout, @NonNull final View view) {
    if (layout == R.layout.project_main_layout) {
      return new ProjectViewHolder(view, delegate);
    }
    return new RewardViewHolder(view, delegate);
  }
}
