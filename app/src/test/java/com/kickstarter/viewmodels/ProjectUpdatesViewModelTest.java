package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import okhttp3.Request;
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

  @Test
  public void testProjectUpdatesViewModel_StartCommentsActivity() {
    final ProjectUpdatesViewModel vm = new ProjectUpdatesViewModel(environment());
    final Project project = ProjectFactory.project();

    final Request commentsRequest = new Request.Builder()
      .url("https://kck.str/projects/param/param/posts/id/comments")
      .build();

    final TestSubscriber<Update> startCommentsActivity = new TestSubscriber<>();
    vm.outputs.startCommentsActivity().subscribe(startCommentsActivity);

    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));
    vm.inputs.updateCommentsRequest(commentsRequest);

    startCommentsActivity.assertValueCount(1);
  }
}
