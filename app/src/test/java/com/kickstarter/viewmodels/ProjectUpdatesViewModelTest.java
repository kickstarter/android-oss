package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class ProjectUpdatesViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testProjectUpdatesViewModel_LoadsInitialIndexUrl() {
    final ProjectUpdatesViewModel vm = new ProjectUpdatesViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> initialIndexUrl = new TestSubscriber<>();
    vm.outputs.webViewUrl().subscribe(initialIndexUrl);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    initialIndexUrl.assertValues(project.updatesUrl());
  }
}
