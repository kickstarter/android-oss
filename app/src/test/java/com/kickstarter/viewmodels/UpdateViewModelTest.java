package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UpdateFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import androidx.annotation.NonNull;
import okhttp3.Request;
import rx.Observable;
import rx.observers.TestSubscriber;

public final class UpdateViewModelTest extends KSRobolectricTestCase {
  private final Intent defaultIntent = new Intent()
    .putExtra(IntentKey.PROJECT, ProjectFactory.project())
    .putExtra(IntentKey.UPDATE, UpdateFactory.update());

  @Test
  public void testOpenProjectExternally_whenProjectUrlIsPreview() {
    final Environment environment = environment();
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment);

    final TestSubscriber<String> openProjectExternally = new TestSubscriber<>();
    vm.outputs.openProjectExternally().subscribe(openProjectExternally);

    // Start the intent with a project and update.
    vm.intent(this.defaultIntent);

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?token=beepboop";
    final Request projectRequest = new Request.Builder()
      .url(url)
      .build();

    vm.inputs.goToProjectRequest(projectRequest);

    openProjectExternally.assertValue(url + "&ref=update");
  }

  @Test
  public void testUpdateViewModel_LoadsWebViewUrl() {
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment());
    final Update update = UpdateFactory.update();

    final String anotherUpdateUrl = "https://kck.str/projects/param/param/posts/next-id";

    final Request anotherUpdateRequest = new Request.Builder()
      .url(anotherUpdateUrl)
      .build();

    final TestSubscriber<String> webViewUrl = new TestSubscriber<>();
    vm.outputs.webViewUrl().subscribe(webViewUrl);

    // Start the intent with a project and update.
    vm.intent(this.defaultIntent);

    // Initial update's url emits.
    webViewUrl.assertValues(update.urls().web().update());

    // Make a request for another update.
    vm.inputs.goToUpdateRequest(anotherUpdateRequest);

    // New update url emits.
    webViewUrl.assertValues(update.urls().web().update(), anotherUpdateUrl);
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

    // Start the intent with a project and update.
    vm.intent(new Intent()
      .putExtra(IntentKey.PROJECT, ProjectFactory.project())
      .putExtra(IntentKey.UPDATE, update)
    );

    vm.inputs.goToCommentsRequest(commentsRequest);
    startCommentsActivity.assertValues(update);
  }

  @Test
  public void testUpdateViewModel_StartProjectActivity() {
    final Environment environment = environment();
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment);

    final TestSubscriber<Uri> startProjectActivity = new TestSubscriber<>();
    vm.outputs.startProjectActivity().map(uriAndRefTag -> uriAndRefTag.first).subscribe(startProjectActivity);

    // Start the intent with a project and update.
    vm.intent(this.defaultIntent);

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap";
    final Request projectRequest = new Request.Builder()
      .url(url)
      .build();

    vm.inputs.goToProjectRequest(projectRequest);

    startProjectActivity.assertValues(Uri.parse(url));
  }

  @Test
  public void testUpdateViewModel_StartShareIntent() {
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment());

    final User creator = UserFactory.creator().toBuilder().id(278438049).build();
    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();
    final String updatesUrl = "https://www.kck.str/projects/" + project.creator().param() + "/" + project.param() + "/posts";

    final int id = 15;

    final Update.Urls.Web web = Update.Urls.Web.builder()
      .update(updatesUrl + "/" + id)
      .likes(updatesUrl + "/likes")
      .build();

    final Update update = UpdateFactory.update()
      .toBuilder()
      .id(id)
      .projectId(project.id())
      .urls(Update.Urls.builder().web(web).build())
      .build();

    final TestSubscriber<Pair<Update, String>> startShareIntent = new TestSubscriber<>();
    vm.outputs.startShareIntent().subscribe(startShareIntent);

    // Start the intent with a project and update.
    vm.intent(new Intent()
      .putExtra(IntentKey.PROJECT, ProjectFactory.project())
      .putExtra(IntentKey.UPDATE, update)
    );
    vm.inputs.shareIconButtonClicked();

    final String expectedShareUrl = "https://www.kck.str/projects/" + project.creator().param() +
      "/" + project.param() + "/posts/" + id + "?ref=android_update_share";
    startShareIntent.assertValue(Pair.create(update, expectedShareUrl));
  }

  @Test
  public void testUpdateViewModel_UpdateSequence() {
    final Update initialUpdate = UpdateFactory.update().toBuilder().sequence(1).build();
    final Update anotherUpdate = UpdateFactory.update().toBuilder().sequence(2).build();

    final Request anotherUpdateRequest = new Request.Builder()
      .url("https://kck.str/projects/param/param/posts/id")
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Update> fetchUpdate(final @NonNull String projectParam, final @NonNull String updateParam) {
        return Observable.just(anotherUpdate);
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment);

    final TestSubscriber<String> updateSequence = new TestSubscriber<>();
    vm.outputs.updateSequence().subscribe(updateSequence);

    // Start the intent with a project and update.
    vm.intent(new Intent()
      .putExtra(IntentKey.PROJECT, ProjectFactory.project())
      .putExtra(IntentKey.UPDATE, initialUpdate)
    );

    // Initial update's sequence number emits.
    updateSequence.assertValues(NumberUtils.format(initialUpdate.sequence()));

    vm.inputs.goToUpdateRequest(anotherUpdateRequest);

    // New sequence should emit for new update page.
    updateSequence.assertValues(NumberUtils.format(initialUpdate.sequence()), NumberUtils.format(anotherUpdate.sequence()));
  }

  @Test
  public void testUpdateViewModel_WebViewUrl() {
    final UpdateViewModel.ViewModel vm = new UpdateViewModel.ViewModel(environment());
    final Update update = UpdateFactory.update();

    final TestSubscriber<String> webViewUrl = new TestSubscriber<>();
    vm.outputs.webViewUrl().subscribe(webViewUrl);

    // Start the intent with a project and update.
    vm.intent(new Intent()
      .putExtra(IntentKey.PROJECT, ProjectFactory.project())
      .putExtra(IntentKey.UPDATE, update)
    );

    // Initial update index url emits.
    webViewUrl.assertValues(update.urls().web().update());
  }
}
