package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public final class ProjectIntentActionTest extends KSRobolectricTestCase {
  @Test
  public void emitsFromProjectParamExtra() {
    final Intent intent = new Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee");

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    final ProjectIntentAction intentAction = new ProjectIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValueCount(1);
  }

  @Test
  public void emitsTwiceFromProjectExtra() {
    final Project project = ProjectFactory.project();
    final Intent intent = new Intent().putExtra(IntentKey.PROJECT, project);

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    final ProjectIntentAction intentAction = new ProjectIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValueCount(2);
  }

  @Test
  public void emitsFromKsrProjectUri() {
    final Uri uri = Uri.parse("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    final ProjectIntentAction intentAction = new ProjectIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValueCount(1);
  }

  @Test
  public void doesNotEmitFromHttpsProjectUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    final ProjectIntentAction intentAction = new ProjectIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertNoValues();
  }
}

