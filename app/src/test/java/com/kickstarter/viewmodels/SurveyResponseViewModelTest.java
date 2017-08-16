package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;

import org.junit.Test;

import okhttp3.Request;
import rx.Observable;
import rx.observers.TestSubscriber;

public class SurveyResponseViewModelTest extends KSRobolectricTestCase {
  private SurveyResponseViewModel.ViewModel vm;
  private final TestSubscriber<Project> project = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new SurveyResponseViewModel.ViewModel(environment);
    this.vm.outputs.startProjectActivity().map(PairUtils::first).subscribe(this.project);
  }

  @Test
  public void testStartProjectActivity() {
    final Project project = ProjectFactory.project().toBuilder().slug("heyo").build();

    final Request projectRequest = new Request.Builder()
      .url("https://kck.str/projects/param/heyo")
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<Project> fetchProject(final @NonNull String param) {
        return Observable.just(project);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.inputs.goToProjectRequest(projectRequest);

    this.project.assertValues(project);
  }
}
