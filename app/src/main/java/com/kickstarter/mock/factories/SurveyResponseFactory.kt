package com.kickstarter.mock.factories;

import com.kickstarter.models.SurveyResponse;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;

public final class SurveyResponseFactory {
  private SurveyResponseFactory() {}

  public static @NonNull SurveyResponse surveyResponse() {
    final String surveyUrl = "https://www.kickstarter.com/surveys/" + IdFactory.id();

    final SurveyResponse.Urls.Web web = SurveyResponse.Urls.Web.builder()
      .survey(surveyUrl)
      .build();

    final SurveyResponse.Urls urlsEnvelope = SurveyResponse.Urls.builder()
      .web(web)
      .build();

    return SurveyResponse.builder()
      .answeredAt(new DateTime().minusDays(10))
      .id(IdFactory.id())
      .project(ProjectFactory.allTheWayProject())
      .urls(urlsEnvelope)
      .build();
  }
}
