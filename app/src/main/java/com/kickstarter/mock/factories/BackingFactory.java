package com.kickstarter.mock.factories;

import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;

public final class BackingFactory {
  private BackingFactory() {}

  public static @NonNull Backing backing() {
    return backing(ProjectFactory.project(), UserFactory.user());
  }

  public static @NonNull Backing backing(final @NonNull User backer) {
    return backing(ProjectFactory.project(), backer, RewardFactory.reward());
  }

  public static @NonNull Backing backing(final @NonNull Project project, final @NonNull User backer) {
    return backing(project, backer, RewardFactory.reward());
  }

  public static @NonNull Backing backing(final @NonNull Project project, final @NonNull User backer, final @NonNull Reward reward) {
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

  public static Backing backing(final @NonNull String status) {
    return backing()
      .toBuilder()
      .status(status)
      .build();
  }
}
