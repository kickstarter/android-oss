package com.kickstarter.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.SurveyResponse;

import org.joda.time.DateTime;

public final class SurveyResponseFactory {
  private SurveyResponseFactory() {}

  public static @NonNull SurveyResponse surveyResponse() {
    final String surveyUrl = "https://www.kickstarter.com/surveys/" + IdFactory.id();

    SurveyResponse.UrlsEnvelope.WebEnvelope webEnvelope = SurveyResponse.UrlsEnvelope.WebEnvelope.builder()
      .survey(surveyUrl)
      .build();

    SurveyResponse.UrlsEnvelope urlsEnvelope = SurveyResponse.UrlsEnvelope.builder()
      .webEnvelope(webEnvelope)
      .build();

    return SurveyResponse.builder()
      .answeredAt(new DateTime().minusDays(10))
      .id(IdFactory.id())
      .project(ProjectFactory.allTheWayProject())
      .urlsEnvelope(urlsEnvelope)
      .build();
  }
}
