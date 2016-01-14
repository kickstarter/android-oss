package com.kickstarter.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import org.joda.time.DateTime;

public final class ProjectFactory {
  public static @NonNull Project project() {
    final User creator = UserFactory.creator();
    final String slug = "slug-1";
    final String projectUrl = "https://www.kickstarter.com/projects/" + String.valueOf(creator.id()) + "/" + slug;

    final Project.Urls.Web web = Project.Urls.Web.builder()
      .project(projectUrl)
      .rewards(projectUrl + "/rewards")
      .build();

    return Project.builder()
      .backersCount(100)
      .blurb("Some blurb")
      .creator(UserFactory.creator())
      .country("US")
      .createdAt(DateTime.now())
      .currency("USD")
      .currencySymbol("$")
      .currencyTrailingCode(true)
      .goal(100.0f)
      .id(1_2345_6789)
      .pledged(50.0f)
      .name("Some Name")
      .state(Project.STATE_LIVE)
      .staticUsdRate(1.0f)
      .slug(slug)
      .updatedAt(DateTime.now())
      .urls(Project.Urls.builder().web(web).build())
      .launchedAt(new DateTime().minusDays(10))
      .deadline(new DateTime().plusDays(10))
      .build();
  }

  public static @NonNull Project halfWayProject() {
    return project()
      .toBuilder()
      .goal(100.0f)
      .pledged(50.0f)
      .build();
  }

  public static @NonNull Project allTheWayProject() {
    return project()
      .toBuilder()
      .goal(100.0f)
      .pledged(100.0f)
      .build();
  }

  public static @NonNull Project doubledGoalProject() {
    return project()
      .toBuilder()
      .goal(100.0f)
      .pledged(200.0f)
      .build();
  }

  public static @NonNull Project caProject() {
    return project()
      .toBuilder()
      .country("CA")
      .currencySymbol("$")
      .currency("CAD")
      .staticUsdRate(0.75f)
      .build();
  }

  public static @NonNull Project ukProject() {
    return project()
      .toBuilder()
      .country("UK")
      .currencySymbol("£")
      .currency("GBP")
      .staticUsdRate(1.5f)
      .build();
  }
}
