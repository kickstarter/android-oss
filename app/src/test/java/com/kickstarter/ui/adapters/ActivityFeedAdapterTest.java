package com.kickstarter.ui.adapters;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ActivityFactory;
import com.kickstarter.mock.factories.SurveyResponseFactory;
import com.kickstarter.models.Activity;
import com.kickstarter.models.SurveyResponse;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActivityFeedAdapterTest extends KSRobolectricTestCase {
  private ActivityFeedAdapter adapter = new ActivityFeedAdapter(null);

  @Test
  public void justActivities() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();

    this.adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));

    final List<List<Activity>> data = Arrays.asList(
      Collections.emptyList(),
      Collections.emptyList(),
      Collections.emptyList(),
      Collections.emptyList(),
      Arrays.asList(
        activity0,
        activity1,
        activity2
      )
    );

    Assert.assertEquals(data, this.adapter.sections());
  }

  @Test
  public void loggedInWithActivities() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();

    this.adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));
    this.adapter.showLoggedInEmptyState(true);

    final List<List<Object>> data = Arrays.asList(
      Collections.singletonList(true),
      Collections.emptyList(),
      Collections.emptyList(),
      Collections.emptyList(),
      Arrays.asList(
        activity0,
        activity1,
        activity2
      )
    );

    Assert.assertEquals(data, this.adapter.sections());
  }

  @Test
  public void loggedOutWithActivities() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();

    this.adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));
    this.adapter.showLoggedOutEmptyState(true);

    final List<List<Object>> data = Arrays.asList(
      Collections.emptyList(),
      Collections.singletonList(false),
      Collections.emptyList(),
      Collections.emptyList(),
      Arrays.asList(
        activity0,
        activity1,
        activity2
      )
    );

    Assert.assertEquals(data, this.adapter.sections());
  }

  @Test
  public void loggedInWithActivitiesAndSurveys() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();
    final SurveyResponse surveyResponse0 = SurveyResponseFactory.surveyResponse();
    final SurveyResponse surveyResponse1 = SurveyResponseFactory.surveyResponse();

    this.adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));
    this.adapter.takeSurveys(Arrays.asList(surveyResponse0, surveyResponse1));
    this.adapter.showLoggedInEmptyState(true);

    final List<List<Object>> data = Arrays.asList(
      Collections.singletonList(true),
      Collections.emptyList(),
      Collections.singletonList(2),
      Arrays.asList(
        surveyResponse0,
        surveyResponse1
      ),
      Arrays.asList(
        activity0,
        activity1,
        activity2
      )
    );

    Assert.assertEquals(data, this.adapter.sections());
  }
}
