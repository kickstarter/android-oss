package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.PhotoFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;

import org.junit.Before;
import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ProjectSearchResultHolderViewModelTest extends KSRobolectricTestCase {
  private ProjectSearchResultHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> projectImage = new TestSubscriber<>();
  private final TestSubscriber<String> projectName = new TestSubscriber<>();
  private final TestSubscriber<Pair<Integer, Integer>> projectStats = new TestSubscriber<>();
  private Project project;

  @Before
  public void setUpEnvironment() {
    this.vm = new ProjectSearchResultHolderViewModel.ViewModel(environment());

    this.vm.outputs.projectImage().subscribe(this.projectImage);
    this.vm.outputs.projectName().subscribe(this.projectName);
    this.vm.outputs.projectStats().subscribe(this.projectStats);
  }

  @Test
  public void testEmitsProjectImage() {
    project = ProjectFactory.project()
      .toBuilder()
      .photo(
        PhotoFactory.photo()
          .toBuilder()
          .med("http://www.kickstarter.com/med.jpg")
          .build()
      )
      .build();
    this.vm.inputs.configureWith(new ProjectSearchResultHolderViewModel.Data(project, false));

    this.projectImage.assertValues("http://www.kickstarter.com/med.jpg");
  }

  // FIXME: do the remaining tests
  // FIXME: test the case of featured (check for the large photo)
  // FIXME: add tests for new outputs

  @Test
  public void testEmitsProjectName() {
    this.projectName.assertValues(project.name());
  }

  @Test
  public void testEmits() {
    this.projectName.assertValues(project.name());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testProjectStats() {
    this.projectStats.assertValues(new Pair<>(50, 10));
  }
}
