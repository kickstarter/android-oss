package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UpdateFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import okhttp3.Request;
import rx.Observable;
import rx.observers.TestSubscriber;

public class UpdateViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testUpdateViewModel_LoadsInitialUpdateUrl() {
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment());
    final Update update = UpdateFactory.update();

    final TestSubscriber<String> initialUpdateUrl = new TestSubscriber<>();
    vm.outputs.webViewUrl().subscribe(initialUpdateUrl);

    vm.intent(new Intent().putExtra(IntentKey.UPDATE, update));
    initialUpdateUrl.assertValues(update.urls().web().update());
  }

  @Test
  public void testUpdateViewModel_StartCommentsActivity() {
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment());
    final Update update = UpdateFactory.update();

    final Request commentsRequest = new Request.Builder()
      .url("https://kck.str/projects/param/param/posts/id/comments")
      .build();

    final TestSubscriber<Update> startCommentsActivity = new TestSubscriber<>();
    vm.outputs.startCommentsActivity().subscribe(startCommentsActivity);

    vm.intent(new Intent().putExtra(IntentKey.UPDATE, update));

    vm.inputs.goToCommentsRequest(commentsRequest);
    startCommentsActivity.assertValues(update);
  }

  @Test
  public void testUpdateViewModel_StartProjectActivity() {
    final Update update = UpdateFactory.update()
      .toBuilder()
      .projectId(1234)
      .build();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .id(update.projectId())
      .build();

    final Request projectRequest = new Request.Builder()
      .url("https://kck.str/projects/param/param")
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Project> fetchProject(@NonNull String param) {
        return Observable.just(project);
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment);

    final TestSubscriber<Project> startProjectActivity = new TestSubscriber<>();
    vm.outputs.startProjectActivity().map(pr -> pr.first).subscribe(startProjectActivity);

    vm.intent(new Intent().putExtra(IntentKey.UPDATE, update));
    vm.inputs.goToProjectRequest(projectRequest);

    startProjectActivity.assertValues(project);
  }

  @Test
  public void testUpdateViewModel_StartShareIntent() {
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment());

    final Update update = UpdateFactory.update();

    final TestSubscriber<Update> startShareIntent = new TestSubscriber<>();
    vm.outputs.startShareIntent().subscribe(startShareIntent);

    vm.intent(new Intent().putExtra(IntentKey.UPDATE, update));
    vm.inputs.shareIconButtonClicked();

    startShareIntent.assertValues(update);
  }
}
