package com.kickstarter.ui.adapters;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ActivityFactory;
import com.kickstarter.factories.SurveyResponseFactory;
import com.kickstarter.models.Activity;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder;
import com.kickstarter.ui.viewholders.FriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder;
import com.kickstarter.ui.viewholders.UnansweredSurveyViewHolder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActivityFeedAdapterTest extends KSRobolectricTestCase implements ActivityFeedAdapter.Delegate {

  private ActivityFeedAdapter adapter = new ActivityFeedAdapter(this);

  @Test
  public void justActivities() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();

    adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));

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

    Assert.assertEquals(data, adapter.sections());
  }

  @Test
  public void loggedInWithActivities() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();

    adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));
    adapter.showLoggedInEmptyState(true);

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

    Assert.assertEquals(data, adapter.sections());
  }

  @Test
  public void loggedOutWithActivities() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();

    adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));
    adapter.showLoggedOutEmptyState(true);

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

    Assert.assertEquals(data, adapter.sections());
  }

  @Test
  public void loggedInWithActivitiesAndSurveys() throws Exception {
    final Activity activity0 = ActivityFactory.projectStateChangedPositiveActivity();
    final Activity activity1 = ActivityFactory.friendBackingActivity();
    final Activity activity2 = ActivityFactory.projectStateChangedActivity();
    final SurveyResponse surveyResponse0 = SurveyResponseFactory.surveyResponse();
    final SurveyResponse surveyResponse1 = SurveyResponseFactory.surveyResponse();

    adapter.takeActivities(Arrays.asList(activity0, activity1, activity2));
    adapter.takeSurveys(Arrays.asList(surveyResponse0, surveyResponse1));
    adapter.showLoggedInEmptyState(true);

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

    Assert.assertEquals(data, adapter.sections());
  }

  /**
   *  These below methods are required to create instance of adapter, but don't need to do anything
   */

  @Override
  public void emptyActivityFeedDiscoverProjectsClicked(final EmptyActivityFeedViewHolder viewHolder) {

  }
  @Override
  public void emptyActivityFeedLoginClicked(final EmptyActivityFeedViewHolder viewHolder) {

  }
  @Override
  public void projectStateChangedClicked(final ProjectStateChangedViewHolder viewHolder, final Activity activity) {

  }
  @Override
  public void surveyClicked(final UnansweredSurveyViewHolder viewHolder, final SurveyResponse surveyResponse) {

  }
  @Override
  public void friendBackingClicked(final FriendBackingViewHolder viewHolder, final Activity activity) {

  }
  @Override
  public void projectUpdateProjectClicked(final ProjectUpdateViewHolder viewHolder, final Activity activity) {

  }
  @Override
  public void projectUpdateClicked(final ProjectUpdateViewHolder viewHolder, final Activity activity) {

  }
  @Override
  public void projectStateChangedPositiveClicked(final ProjectStateChangedPositiveViewHolder viewHolder, final Activity activity) {

  }
}
