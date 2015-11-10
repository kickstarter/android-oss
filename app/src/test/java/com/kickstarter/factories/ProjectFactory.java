package com.kickstarter.factories;

import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import org.joda.time.DateTime;

public class ProjectFactory {
  public static Project project() {
    final User creator = UserFactory.creator();
    final String slug = "slug-1";
    final String projectUrl = "https://www.kickstarter.com/projects/" + creator.uid() + "/" + slug;

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
      .slug(slug)
      .updatedAt(DateTime.now())
      .urls(Project.Urls.builder().web(web).build())
      .build();
  }

  public static Project halfWayProject() {
    return project()
      .toBuilder()
      .goal(100.0f)
      .pledged(50.0f)
      .build();
  }

  public static Project allTheWayProject() {
    return project()
      .toBuilder()
      .goal(100.0f)
      .pledged(100.0f)
      .build();
  }

  public static Project doubledGoalProject() {
    return project()
      .toBuilder()
      .goal(100.0f)
      .pledged(200.0f)
      .build();
  }
}
