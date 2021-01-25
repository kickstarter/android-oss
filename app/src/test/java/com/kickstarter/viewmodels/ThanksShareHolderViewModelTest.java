package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public final class ThanksShareHolderViewModelTest extends KSRobolectricTestCase {
  private ThanksShareHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> projectName = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> startShare = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, String>> startShareOnFacebook = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> startShareOnTwitter = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ThanksShareHolderViewModel.ViewModel(environment);
    this.vm.outputs.projectName().subscribe(this.projectName);
    this.vm.outputs.startShare().subscribe(this.startShare);
    this.vm.outputs.startShareOnFacebook().subscribe(this.startShareOnFacebook);
    this.vm.outputs.startShareOnTwitter().subscribe(this.startShareOnTwitter);
  }

  @Test
  public void testProjectName() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.configureWith(project);
    this.projectName.assertValues(project.name());
  }

  @Test
  public void testStartShare() {
    setUpEnvironment(environment());

    final Project project = setUpProjectWithWebUrls();
    this.vm.configureWith(project);

    this.vm.inputs.shareClick();
    final String expectedShareUrl = "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_share";
    this.startShare.assertValue(Pair.create("Best Project 2K19", expectedShareUrl));
  }

  @Test
  public void testStartShareOnFacebook() {
    setUpEnvironment(environment());

    final Project project = setUpProjectWithWebUrls();
    this.vm.configureWith(project);

    this.vm.inputs.shareOnFacebookClick();
    final String expectedShareUrl = "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_facebook_share";
    this.startShareOnFacebook.assertValue(Pair.create(project, expectedShareUrl));
  }

  @Test
  public void testStartShareOnTwitter() {
    setUpEnvironment(environment());

    final Project project = setUpProjectWithWebUrls();
    this.vm.configureWith(project);

    this.vm.inputs.shareOnTwitterClick();
    final String expectedShareUrl = "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_twitter_share";
    this.startShareOnTwitter.assertValue(Pair.create("Best Project 2K19", expectedShareUrl));
  }

  private Project setUpProjectWithWebUrls() {
    final int creatorId = 15;
    final User creator = UserFactory.creator()
      .toBuilder()
      .id(creatorId)
      .build();
    final String slug = "best-project-2k19";
    final String projectUrl = "https://www.kck.str/projects/"  + creator.id() + "/" + slug;

    final Project.Urls.Web webUrls = Project.Urls.Web.builder()
      .project(projectUrl)
      .rewards("$projectUrl/rewards")
      .updates("$projectUrl/posts")
      .build();

    return ProjectFactory.project()
      .toBuilder()
      .name("Best Project 2K19")
      .urls(Project.Urls.builder().web(webUrls).build())
      .build();
  }
}
