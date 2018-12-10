package com.kickstarter.mock.factories;

import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;

public final class UpdateFactory {
  private UpdateFactory() {}

  public static Update update() {
    final User creator = UserFactory.creator().toBuilder().id(278438049).build();
    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();
    final String updatesUrl = "https://www.kck.str/projects/" + project.creator().param() + "/" + project.param() + "/posts";

    final Update.Urls.Web web = Update.Urls.Web.builder()
      .update(updatesUrl + "id")
      .likes(updatesUrl + "/likes")
      .build();

    return Update.builder()
      .body("Update body")
      .id(1234)
      .projectId(5678)
      .sequence(11111)
      .title("First update")
      .urls(Update.Urls.builder().web(web).build())
      .build();
  }
}
