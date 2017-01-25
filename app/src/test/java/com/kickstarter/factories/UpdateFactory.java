package com.kickstarter.factories;

import com.kickstarter.models.Project;
import com.kickstarter.models.Update;

public final class UpdateFactory {
  private UpdateFactory() {}

  public static Update update() {
    final Project project = ProjectFactory.project();
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
