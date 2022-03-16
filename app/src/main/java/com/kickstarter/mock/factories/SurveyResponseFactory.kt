package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.ProjectFactory.allTheWayProject
import com.kickstarter.models.SurveyResponse
import org.joda.time.DateTime

object SurveyResponseFactory {
    @JvmStatic
    fun surveyResponse(): SurveyResponse {
        val surveyUrl = "https://www.kickstarter.com/surveys/" + IdFactory.id()

        val web = SurveyResponse.Urls.Web.builder()
            .survey(surveyUrl)
            .build()

        val urlsEnvelope = SurveyResponse.Urls.builder()
            .web(web)
            .build()

        return SurveyResponse.builder()
            .answeredAt(DateTime().minusDays(10))
            .id(IdFactory.id().toLong())
            .project(allTheWayProject())
            .urls(urlsEnvelope)
            .build()
    }
}
