package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;

import org.joda.time.DateTime;

public final class ProjectFactory {
  private ProjectFactory() {}

  public static @NonNull Project project() {
    final User creator = UserFactory.creator();
    final String slug = "slug-1";
    final String projectUrl = "https://www.kickstarter.com/projects/" + String.valueOf(creator.id()) + "/" + slug;

    final Project.Urls.Web web = Project.Urls.Web.builder()
      .project(projectUrl)
      .rewards(projectUrl + "/rewards")
      .updates(projectUrl + "/posts")
      .build();

    return Project.builder()
      .backersCount(100)
      .blurb("Some blurb")
      .category(CategoryFactory.category())
      .creator(UserFactory.creator())
      .country("US")
      .createdAt(DateTime.now())
      .currency("USD")
      .currencySymbol("$")
      .currencyTrailingCode(true)
      .goal(100.0f)
      .id(IdFactory.id())
      .name("Some Name")
      .pledged(50.0f)
      .photo(PhotoFactory.photo())
      .staffPick(false)
      .state(Project.STATE_LIVE)
      .staticUsdRate(1.0f)
      .slug(slug)
      .updatedAt(DateTime.now())
      .urls(Project.Urls.builder().web(web).build())
      .video(VideoFactory.video())
      .launchedAt(new DateTime().minusDays(10))
      .deadline(new DateTime().plusDays(10))
      .build();
  }

  public static @NonNull Project backedProject() {
    final Project project = project();

    final Reward reward = RewardFactory.reward();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(reward.id())
      .pledgedAt(DateTime.now())
      .projectCountry(project.country())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project backedProjectWithRewardLimited() {
    final Project project = project();

    final Reward reward = RewardFactory.limited();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(reward.id())
      .pledgedAt(DateTime.now())
      .projectCountry(project.country())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project backedProjectWithRewardLimitReached() {
    final Project project = project();

    final Reward reward = RewardFactory.limitReached();

    final Backing backing = Backing.builder()
      .amount(10.0f)
      .backerId(IdFactory.id())
      .id(IdFactory.id())
      .sequence(1)
      .reward(reward)
      .rewardId(reward.id())
      .pledgedAt(DateTime.now())
      .projectCountry(project.country())
      .projectId(project.id())
      .shippingAmount(0.0f)
      .status(Backing.STATUS_PLEDGED)
      .build();

    return project
      .toBuilder()
      .backing(backing)
      .isBacking(true)
      .build();
  }

  public static @NonNull Project halfWayProject() {
    return project()
      .toBuilder()
      .name("halfwayProject")
      .goal(100.0f)
      .pledged(50.0f)
      .build();
  }

  public static @NonNull Project allTheWayProject() {
    return project()
      .toBuilder()
      .name("allTheWayProject")
      .goal(100.0f)
      .pledged(100.0f)
      .build();
  }

  public static @NonNull Project doubledGoalProject() {
    return project()
      .toBuilder()
      .name("doubledGoalProject")
      .goal(100.0f)
      .pledged(200.0f)
      .build();
  }

  public static @NonNull Project failedProject() {
    return project()
      .toBuilder()
      .name("failedProject")
      .state(Project.STATE_FAILED)
      .build();
  }

  public static @NonNull Project caProject() {
    return project()
      .toBuilder()
      .name("caProject")
      .country("CA")
      .currencySymbol("$")
      .currency("CAD")
      .staticUsdRate(0.75f)
      .build();
  }

  public static @NonNull Project ukProject() {
    return project()
      .toBuilder()
      .name("ukProject")
      .country("UK")
      .currencySymbol("£")
      .currency("GBP")
      .staticUsdRate(1.5f)
      .build();
  }

  public static @NonNull Project almostCompletedProject() {
    return project()
      .toBuilder()
      .name("almostCompleteProject")
      .deadline(new DateTime().plusDays(1))
      .build();
  }

  public static @NonNull Project successfulProject() {
    return project()
      .toBuilder()
      .name("successfulProject")
      .deadline(new DateTime().minus(2))
      .state(Project.STATE_SUCCESSFUL)
      .build();
  }

  public static Project featured() {
    return project()
      .toBuilder()
      .name("featuredProject")
      .featuredAt(new DateTime())
      .build();
  }

  public static Project saved() {
    return project()
      .toBuilder()
      .name("savedProject")
      .isStarred(true)
      .build();
  }

  public static Project staffPick() {
    return project()
      .toBuilder()
      .name("staffPickProject")
      .staffPick(true)
      .build();
  }
}
