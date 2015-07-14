package com.kickstarter.factories;

import com.kickstarter.models.Project;

public class ProjectFactory {
  public static Project project() {
    Project project = new Project();
    project.urls = new Project.Urls();
    project.urls.web = new Project.Urls.Web();
    project.creator = UserFactory.creator();
    project.name = "Some Name";
    project.slug = "slug-1";
    project.urls.web.project = "https://www.kickstarter.com/projects/" + project.creator().uid() + "/" + project.slug;

    return project;
  }
}
