package com.kickstarter.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;

import org.joda.time.DateTime;

public final class BackingFactory {
  private BackingFactory() {}

  public static @NonNull Backing backing() {
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward();
    final User backer = UserFactory.user();

    return Backing.builder()
      .amount(10.0f)
      .backer(backer)
      .backerId(backer.id())
      .id(IdFactory.id())
      .pledgedAt(DateTime.now())
      .project(project)
      .projectCountry(project.country())
      .projectId(project.id())
      .reward(reward)
      .rewardId(reward.id())
      .sequence(1)
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();
  }
}
