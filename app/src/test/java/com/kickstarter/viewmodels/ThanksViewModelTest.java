package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ThanksViewModelTest extends KSRobolectricTestCase {
  @Test
  public void testThanksViewModel_project() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Project> projectTest = new TestSubscriber<>();
    vm.outputs.project().subscribe(projectTest);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));
    projectTest.assertValues(project);
  }
}
