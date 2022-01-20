package com.kickstarter.services

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.environmentalCommitmentTransformer
import com.kickstarter.services.transformers.projectFaqTransformer
import com.kickstarter.services.transformers.userTransformer
import fragment.EnvironmentalCommitment
import fragment.Faq
import fragment.User
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import type.EnvironmentalCommitmentCategory

class GraphQLTransformersTest : KSRobolectricTestCase() {

    @Test
    fun testProjectFaqTransformer() {
        val faq: Faq = mock(Faq::class.java)
        `when`(faq.id()).thenReturn("UHJvamVjdEZhcS0zODE3Mzk=")
        `when`(faq.answer()).thenReturn("answer")
        `when`(faq.question()).thenReturn("question")

        val projectFaq = projectFaqTransformer(faq)

        assertTrue(projectFaq.id == decodeRelayId(faq.id()))
        assertTrue(projectFaq.answer == "answer")
        assertTrue(projectFaq.question == "question")
        assertTrue(projectFaq.createdAt == null)

        val faq2: Faq = mock(Faq::class.java)
        `when`(faq2.id()).thenReturn("")
        `when`(faq2.answer()).thenReturn("")
        `when`(faq2.question()).thenReturn("")
        `when`(faq2.createdAt()).thenReturn(null)

        val projectFaq2 = projectFaqTransformer(faq2)
        assertTrue(projectFaq2.id == -1L)
        assertTrue(projectFaq2.createdAt == null)

        val date = DateTime.now().plusMillis(300)
        val faq3: Faq = mock(Faq::class.java)
        `when`(faq3.id()).thenReturn("")
        `when`(faq3.answer()).thenReturn("")
        `when`(faq3.question()).thenReturn("")
        `when`(faq3.createdAt()).thenReturn(date)

        val projectFaq3 = projectFaqTransformer(faq3)
        assertTrue(projectFaq3.createdAt == date)
    }

    @Test
    fun testEnvironmentalCommitmentTransformer() {
        val envCom: EnvironmentalCommitment = mock(EnvironmentalCommitment::class.java)
        `when`(envCom.id()).thenReturn("RW52aXJvbm1lbnRhbENvbW1pdG1lbnQtMTMzNTMy")
        `when`(envCom.description()).thenReturn("desc")
        `when`(envCom.commitmentCategory()).thenReturn(EnvironmentalCommitmentCategory.ENVIRONMENTALLY_FRIENDLY_FACTORIES)

        val projEnvConv = environmentalCommitmentTransformer(envCom)
        assertTrue(projEnvConv.id == 133532L)
        assertTrue(projEnvConv.description == "desc")
        assertTrue(projEnvConv.category == EnvironmentalCommitmentCategory.ENVIRONMENTALLY_FRIENDLY_FACTORIES.name)

        val envCom2: EnvironmentalCommitment = mock(EnvironmentalCommitment::class.java)
        `when`(envCom2.id()).thenReturn("")
        `when`(envCom2.description()).thenReturn("")
        `when`(envCom2.commitmentCategory()).thenReturn(EnvironmentalCommitmentCategory.LONG_LASTING_DESIGN)

        val projEnvConv2 = environmentalCommitmentTransformer(envCom2)
        assertTrue(projEnvConv2.id == -1L)
        assertTrue(projEnvConv2.description == "")
        assertTrue(projEnvConv2.category == EnvironmentalCommitmentCategory.LONG_LASTING_DESIGN.name)
    }

    @Test
    fun testUserTransformer() {
        val userFragment = mock(User::class.java)
        `when`(userFragment.id()).thenReturn("VXNlci0yMzc5NjEyNDM=")
        `when`(userFragment.name()).thenReturn("Brotherwise Games")
        `when`(userFragment.imageUrl()).thenReturn("https://ksr-qa-ugc.imgix.net/assets/005/791/327/f120c4cfe49495849b526b2cc6da44f9_original.png")
        `when`(userFragment.isCreator).thenReturn(true)
        `when`(userFragment.chosenCurrency()).thenReturn(null)

        val user = userTransformer(userFragment)
        assertTrue(user.id() == 237961243L)
        assertTrue(user.name() == "Brotherwise Games")
        assertTrue(user.avatar().medium() == "https://ksr-qa-ugc.imgix.net/assets/005/791/327/f120c4cfe49495849b526b2cc6da44f9_original.png")
    }
}
