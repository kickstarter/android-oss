package com.kickstarter.ui.adapters;

import android.util.Pair;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.libs.utils.RewardUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.data.ProjectData;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectViewHolder;
import com.kickstarter.ui.viewholders.RewardViewHolder;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public final class ProjectAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectViewHolder.Delegate {}

  public ProjectAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_main_layout;
    } else {
      return R.layout.reward_view;
    }
  }

  /**
   * Populate adapter data when we know we're working with a ProjectData object.
   */
  public void takeProject(final @NonNull ProjectData projectData, final @NonNull Boolean nativeCheckoutEnabled) {
    sections().clear();
    sections().add(Collections.singletonList(projectData));

    final Project project = projectData.project();
    final List<Reward> rewards = project.rewards();
    if (rewards != null && !nativeCheckoutEnabled) {
      addSection(Observable.from(rewards)
        .filter(RewardUtils::isReward)
        .map(reward -> Pair.create(project, reward))
        .toList().toBlocking().single()
      );
    }
    notifyDataSetChanged();
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.project_main_layout) {
      return new ProjectViewHolder(view, this.delegate);
    }
    return new RewardViewHolder(view);
  }
}
