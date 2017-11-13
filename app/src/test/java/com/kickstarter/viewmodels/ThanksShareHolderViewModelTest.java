package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ThanksShareHolderViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testThanksShareHolderViewModel_projectName() {
    final ThanksShareHolderViewModel.ViewModel vm = new ThanksShareHolderViewModel.ViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> projectNameTest = new TestSubscriber<>();
    vm.outputs.projectName().subscribe(projectNameTest);

    vm.configureWith(project);
    projectNameTest.assertValues(project.name());
  }

  @Test
  public void testThanksShareHolderViewModel_share() {
    final ThanksShareHolderViewModel.ViewModel vm = new ThanksShareHolderViewModel.ViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Project> startShareTest = new TestSubscriber<>();
    vm.outputs.startShare().subscribe(startShareTest);
    final TestSubscriber<Project> startShareOnFacebookTest = new TestSubscriber<>();
    vm.outputs.startShareOnFacebook().subscribe(startShareOnFacebookTest);
    final TestSubscriber<Project> startShareOnTwitterTest = new TestSubscriber<>();
    vm.outputs.startShareOnTwitter().subscribe(startShareOnTwitterTest);

    vm.configureWith(project);

    vm.inputs.shareClick();
    startShareTest.assertValues(project);
    koalaTest.assertValues("Checkout Show Share Sheet");

    vm.inputs.shareOnFacebookClick();
    startShareOnFacebookTest.assertValues(project);
    koalaTest.assertValues("Checkout Show Share Sheet", "Checkout Show Share");

    vm.inputs.shareOnTwitterClick();
    startShareOnTwitterTest.assertValues(project);
    koalaTest.assertValues("Checkout Show Share Sheet", "Checkout Show Share", "Checkout Show Share");
  }
}
