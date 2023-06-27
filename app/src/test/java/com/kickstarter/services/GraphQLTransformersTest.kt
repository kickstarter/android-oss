package com.kickstarter.services

import UserPrivacyQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.environmentalCommitmentTransformer
import com.kickstarter.services.transformers.projectFaqTransformer
import com.kickstarter.services.transformers.updateTransformer
import com.kickstarter.services.transformers.userPrivacyTransformer
import com.kickstarter.services.transformers.userTransformer
import fragment.EnvironmentalCommitment
import fragment.Faq
import fragment.User
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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

    @Test
    fun testUserPrivacyTransformer() {
        val userPrivacyQuery = mock(UserPrivacyQuery.Me::class.java)
        `when`(userPrivacyQuery.name()).thenReturn("Brotherwise Games")
        `when`(userPrivacyQuery.email()).thenReturn("hello@kickstarter.com")
        `when`(userPrivacyQuery.hasPassword()).thenReturn(true)
        `when`(userPrivacyQuery.isCreator).thenReturn(true)
        `when`(userPrivacyQuery.isDeliverable).thenReturn(true)
        `when`(userPrivacyQuery.isEmailVerified).thenReturn(true)
        `when`(userPrivacyQuery.chosenCurrency()).thenReturn(null)

        val userPrivacy = userPrivacyTransformer(userPrivacyQuery)
        assertTrue(userPrivacy.name == "Brotherwise Games")
        assertTrue(userPrivacy.email == "hello@kickstarter.com")
        assertTrue(userPrivacy.hasPassword)
        assertTrue(userPrivacy.isCreator)
        assertTrue(userPrivacy.isDeliverable)
        assertTrue(userPrivacy.chosenCurrency == "USD")
    }

    @Test
    fun testUpdateTransformer() {
        val post = mock(fragment.Post::class.java)
        `when`(post.id()).thenReturn("VXNlci0yMzc5NjEyNDM=")
        `when`(post.title()).thenReturn("Updated Add-on list")
        `when`(post.isPublic).thenReturn(true)
        `when`(post.isVisible).thenReturn(true)
        `when`(post.isLiked).thenReturn(true)
        `when`(post.number()).thenReturn(5)

        val date = DateTime.now().plusMillis(300)
        `when`(post.publishedAt()).thenReturn(date)
        `when`(post.updatedAt()).thenReturn(date)

        val user = updateTransformer(post)
        assertTrue(user.id() == 237961243L)
        assertTrue(user.projectId() == -1L)
        assertTrue(user.title() == "Updated Add-on list")
        assertTrue(user.isPublic() == true)
        assertTrue(user.visible() == true)
        assertTrue(user.hasLiked() == true)
        assertTrue(user.sequence() == 5)
        assertTrue(post.publishedAt() == date)
        assertTrue(post.updatedAt() == date)
    }
}
