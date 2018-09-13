package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.PhotoFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;
import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ProjectSearchResultHolderViewModelTest extends KSRobolectricTestCase {
  private ProjectSearchResultHolderViewModel.ViewModel vm;
  private final TestSubscriber<Project> notifyDelegateOfResultClick = new TestSubscriber<>();
  private final TestSubscriber<String> percentFundedTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Project> projectForDeadlineCountdownUnitTextView = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> projectPhotoUrl = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ProjectSearchResultHolderViewModel.ViewModel(environment);

    this.vm.outputs.notifyDelegateOfResultClick().subscribe(this.notifyDelegateOfResultClick);
    this.vm.outputs.percentFundedTextViewText().subscribe(this.percentFundedTextViewText);
    this.vm.outputs.projectForDeadlineCountdownUnitTextView().subscribe(this.projectForDeadlineCountdownUnitTextView);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.projectPhotoUrl().subscribe(this.projectPhotoUrl);
  }

  @Test
  public void testEmitsProjectImage() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .photo(
        PhotoFactory.photo()
          .toBuilder()
          .med("http://www.kickstarter.com/med.jpg")
          .build()
      )
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, false));

    this.projectPhotoUrl.assertValues("http://www.kickstarter.com/med.jpg");
  }

  @Test
  public void testEmitsFeaturedProjectImage() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .photo(
        PhotoFactory.photo()
          .toBuilder()
          .full("http://www.kickstarter.com/full.jpg")
          .build()
      )
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, true));

    this.projectPhotoUrl.assertValues("http://www.kickstarter.com/full.jpg");
  }

  @Test
  public void testEmitsProjectName() {
    final Project project = ProjectFactory.project();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, true));
    this.projectNameTextViewText.assertValues(project.name());
  }

  @Test
  public void testEmitsProjectStats() {
    final Project project = ProjectFactory.project()
      .toBuilder()
      .pledged(100)
      .goal(200)
      .deadline(new DateTime().plusHours(24 * 10 + 1))
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, true));

    this.percentFundedTextViewText.assertValues(NumberUtils.flooredPercentage(project.percentageFunded()));
    this.projectForDeadlineCountdownUnitTextView.assertValues(project);
  }

  @Test
  public void testEmitsProjectClicked() {
    final Project project = ProjectFactory.project();

    setUpEnvironment(environment());

    this.vm.inputs.configureWith(Pair.create(project, true));
    this.vm.inputs.projectClicked();

    this.notifyDelegateOfResultClick.assertValues(project);
  }
}
