package com.kickstarter.viewmodels;

import android.support.v4.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
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
    project = ProjectFactory.project();
    this.vm.inputs.configureWith(project, false);
    this.vm.outputs.projectImage().subscribe(this.projectImage);
    this.vm.outputs.projectName().subscribe(this.projectName);
    this.vm.outputs.projectStats().subscribe(this.projectStats);
  }

  @Test
  public void testEmitsProjectImage() {
    this.projectImage.assertValues(project.photo().med());
  }

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
