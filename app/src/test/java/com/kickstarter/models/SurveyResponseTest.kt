package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.IdFactory
import com.kickstarter.mock.factories.ProjectFactory
import org.joda.time.DateTime
import org.junit.Test

class SurveyResponseTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val dateTime: DateTime = DateTime.now().plusMillis(300)

        val surveyUrl = "https://www.kickstarter.com/surveys/" + IdFactory.id()

        val project = ProjectFactory.allTheWayProject()

        val web = SurveyResponse.Urls.Web.builder()
            .survey(surveyUrl)
            .build()

        val urlsEnvelope = SurveyResponse.Urls.builder()
            .web(web)
            .build()

        val survey = SurveyResponse.builder()
            .id(1234L)
            .answeredAt(dateTime)
            .project(project)
            .urls(urlsEnvelope).build()

        assertEquals(survey.id(), 1234L)
        assertEquals(survey.answeredAt(), dateTime)
        assertEquals(survey.project(), project)
        assertEquals(survey.urls(), urlsEnvelope)
        assertEquals(survey.urls()?.web(), web)
        assertEquals(survey.urls()?.web()?.survey(), surveyUrl)
    }

    @Test
    fun testSurvey_equalFalse() {
        val survey = SurveyResponse.builder().build()
        val survey2 = SurveyResponse.builder().project(ProjectFactory.backedProject()).build()
        val survey3 = SurveyResponse.builder().project(ProjectFactory.allTheWayProject()).id(5678L).build()
        val survey4 = SurveyResponse.builder().project(ProjectFactory.allTheWayProject()).build()

        assertFalse(survey == survey2)
        assertFalse(survey == survey3)
        assertFalse(survey == survey4)

        assertFalse(survey3 == survey2)
        assertFalse(survey3 == survey4)
    }

    @Test
    fun testSurvey_equalTrue() {
        val survey1 = SurveyResponse.builder().build()
        val survey2 = SurveyResponse.builder().build()

        assertEquals(survey1, survey2)
    }
}
