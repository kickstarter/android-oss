package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ThanksViewModelTest extends KSRobolectricTestCase {
  @Test
  public void testThanksViewModel_projectName() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> projectNameTest = new TestSubscriber<>();
    vm.outputs.projectName().subscribe(projectNameTest);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));
    projectNameTest.assertValues(project.name());
  }

  @Test
  public void testThanksViewModel_share() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Project> startShareTest = new TestSubscriber<>();
    vm.outputs.startShare().subscribe(startShareTest);
    final TestSubscriber<Project> startShareOnFacebookTest = new TestSubscriber<>();
    vm.outputs.startShareOnFacebook().subscribe(startShareOnFacebookTest);
    final TestSubscriber<Project> startShareOnTwitterTest = new TestSubscriber<>();
    vm.outputs.startShareOnTwitter().subscribe(startShareOnTwitterTest);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()));

    vm.inputs.shareClick();
    startShareTest.assertValues(project);

    vm.inputs.shareOnFacebookClick();
    startShareOnFacebookTest.assertValues(project);

    vm.inputs.shareOnTwitterClick();
    startShareOnTwitterTest.assertValues(project);
  }

  @Test
  public void testThanksViewModel_startDiscovery() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Category category = CategoryFactory.category();

    final TestSubscriber<DiscoveryParams> startDiscoveryTest = new TestSubscriber<>();
    vm.outputs.startDiscovery().subscribe(startDiscoveryTest);

    vm.inputs.categoryClick(null, category);
    startDiscoveryTest.assertValues(DiscoveryParams.builder().category(category).build());
  }

  @Test
  public void testThanksViewModel_startProject() {
    final ThanksViewModel vm = new ThanksViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Project> startProjectTest = new TestSubscriber<>();
    vm.outputs.startProject().subscribe(startProjectTest);

    vm.inputs.projectClick(null, project);
    startProjectTest.assertValues(project);
  }
}
