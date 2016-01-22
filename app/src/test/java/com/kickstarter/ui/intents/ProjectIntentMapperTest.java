package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.intentmappers.ProjectIntentMapper;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class ProjectIntentMapperTest extends KSRobolectricTestCase {
  @Test
  public void testProject_emitsFromProjectParamExtra() {
    final Intent intent = new Intent().putExtra(IntentKey.PROJECT_PARAM, "skull-graphic-tee");

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    ProjectIntentMapper.project(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValueCount(1);
  }

  @Test
  public void testProject_emitsTwiceFromProjectExtra() {
    final Project project = ProjectFactory.project();
    final Intent intent = new Intent().putExtra(IntentKey.PROJECT, project);

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    ProjectIntentMapper.project(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValueCount(2);
  }

  @Test
  public void testProject_emitsFromKsrProjectUri() {
    final Uri uri = Uri.parse("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    ProjectIntentMapper.project(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValueCount(1);
  }

  @Test
  public void testProject_doesNotEmitFromHttpsProjectUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<Project> resultTest = TestSubscriber.create();
    ProjectIntentMapper.project(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertNoValues();
  }

  @Test
  public void testRefTag_emitsFromRefTag() {
    final RefTag refTag = RefTag.from("test");
    final Intent intent = new Intent().putExtra(IntentKey.REF_TAG, refTag);

    final TestSubscriber<RefTag> resultTest = TestSubscriber.create();
    ProjectIntentMapper.refTag(intent).subscribe(resultTest);

    resultTest.assertValue(refTag);
  }

  @Test
  public void testRefTag_emitsNullWithNoRefTag() {
    final Intent intent = new Intent();

    final TestSubscriber<RefTag> resultTest = TestSubscriber.create();
    ProjectIntentMapper.refTag(intent).subscribe(resultTest);

    resultTest.assertValue(null);
  }
}

