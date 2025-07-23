package com.kickstarter.services

import com.kickstarter.FetchProjectRewardsQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.UserPrivacyQuery
import com.kickstarter.fragment.AiDisclosure
import com.kickstarter.fragment.Amount
import com.kickstarter.fragment.EnvironmentalCommitment
import com.kickstarter.fragment.Faq
import com.kickstarter.fragment.Reward
import com.kickstarter.fragment.Reward.AllowedAddons
import com.kickstarter.fragment.User
import com.kickstarter.services.transformers.aiDisclosureTransformer
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.environmentalCommitmentTransformer
import com.kickstarter.services.transformers.projectFaqTransformer
import com.kickstarter.services.transformers.rewardTransformer
import com.kickstarter.services.transformers.simpleShippingRuleTransformer
import com.kickstarter.services.transformers.updateTransformer
import com.kickstarter.services.transformers.userPrivacyTransformer
import com.kickstarter.services.transformers.userTransformer
import com.kickstarter.type.EnvironmentalCommitmentCategory
import com.kickstarter.type.RewardType
import com.kickstarter.type.ShippingPreference
import org.joda.time.DateTime
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GraphQLTransformersTest : KSRobolectricTestCase() {

    @Test
    fun testProjectFaqTransformer() {
        val faq: Faq = mock(Faq::class.java)
        `when`(faq.id).thenReturn("UHJvamVjdEZhcS0zODE3Mzk=")
        `when`(faq.answer).thenReturn("answer")
        `when`(faq.question).thenReturn("question")

        val projectFaq = projectFaqTransformer(faq)

        assertTrue(projectFaq.id == decodeRelayId(faq.id))
        assertTrue(projectFaq.answer == "answer")
        assertTrue(projectFaq.question == "question")
        assertTrue(projectFaq.createdAt == null)

        val faq2: Faq = mock(Faq::class.java)
        `when`(faq2.id).thenReturn("")
        `when`(faq2.answer).thenReturn("")
        `when`(faq2.question).thenReturn("")
        `when`(faq2.createdAt).thenReturn(null)

        val projectFaq2 = projectFaqTransformer(faq2)
        assertTrue(projectFaq2.id == -1L)
        assertTrue(projectFaq2.createdAt == null)

        val date = DateTime.now().plusMillis(300)
        val faq3: Faq = mock(Faq::class.java)
        `when`(faq3.id).thenReturn("")
        `when`(faq3.answer).thenReturn("")
        `when`(faq3.question).thenReturn("")
        `when`(faq3.createdAt).thenReturn(date)

        val projectFaq3 = projectFaqTransformer(faq3)
        assertTrue(projectFaq3.createdAt == date)
    }

    @Test
    fun testEnvironmentalCommitmentTransformer() {
        val envCom: EnvironmentalCommitment = mock(EnvironmentalCommitment::class.java)
        `when`(envCom.id).thenReturn("RW52aXJvbm1lbnRhbENvbW1pdG1lbnQtMTMzNTMy")
        `when`(envCom.description).thenReturn("desc")
        `when`(envCom.commitmentCategory).thenReturn(EnvironmentalCommitmentCategory.environmentally_friendly_factories)

        val projEnvConv = environmentalCommitmentTransformer(envCom)
        assertTrue(projEnvConv.id == 133532L)
        assertTrue(projEnvConv.description == "desc")
        assertTrue(projEnvConv.category == EnvironmentalCommitmentCategory.environmentally_friendly_factories.name)

        val envCom2: EnvironmentalCommitment = mock(EnvironmentalCommitment::class.java)
        `when`(envCom2.id).thenReturn("")
        `when`(envCom2.description).thenReturn("")
        `when`(envCom2.commitmentCategory).thenReturn(EnvironmentalCommitmentCategory.long_lasting_design)

        val projEnvConv2 = environmentalCommitmentTransformer(envCom2)
        assertTrue(projEnvConv2.id == -1L)
        assertTrue(projEnvConv2.description == "")
        assertTrue(projEnvConv2.category == EnvironmentalCommitmentCategory.long_lasting_design.name)
    }

    @Test
    fun testAiDisclosureTransformer() {
        val aiDisclosureMockComplete: AiDisclosure = mock(AiDisclosure::class.java)
        `when`(aiDisclosureMockComplete.id).thenReturn("QWlEaXNjbG9zdXJlLTE=")
        `when`(aiDisclosureMockComplete.fundingForAiAttribution).thenReturn(true)
        `when`(aiDisclosureMockComplete.fundingForAiConsent).thenReturn(true)
        `when`(aiDisclosureMockComplete.fundingForAiOption).thenReturn(true)
        `when`(aiDisclosureMockComplete.generatedByAiConsent).thenReturn("Some text here")
        `when`(aiDisclosureMockComplete.generatedByAiDetails).thenReturn("Some other details text here")
        `when`(aiDisclosureMockComplete.otherAiDetails).thenReturn("some other details here")
        val aiDisclosure = aiDisclosureTransformer(aiDisclosureGraph = aiDisclosureMockComplete)

        assertTrue(aiDisclosure.id == 1L)
        assertTrue(aiDisclosure.fundingForAiAttribution)
        assertTrue(aiDisclosure.fundingForAiConsent)
        assertTrue(aiDisclosure.fundingForAiOption)

        assertEquals(aiDisclosure.generatedByAiConsent, "Some text here")
        assertEquals(aiDisclosure.generatedByAiDetails, "Some other details text here")
        assertEquals(aiDisclosure.otherAiDetails, "some other details here")

        val aiDisclosureMockPartial: AiDisclosure = mock(AiDisclosure::class.java)
        `when`(aiDisclosureMockPartial.id).thenReturn("")
        `when`(aiDisclosureMockPartial.fundingForAiAttribution).thenReturn(false)
        `when`(aiDisclosureMockPartial.generatedByAiConsent).thenReturn("Some more text here")
        val aiDisclosure1 = aiDisclosureTransformer(aiDisclosureGraph = aiDisclosureMockPartial)

        assertTrue(aiDisclosure1.id == -1L)
        assertFalse(aiDisclosure1.fundingForAiAttribution)
        assertFalse(aiDisclosure1.fundingForAiConsent)
        assertFalse(aiDisclosure1.fundingForAiOption)

        assertEquals(aiDisclosure1.generatedByAiConsent, "Some more text here")
        assertEquals(aiDisclosure1.generatedByAiDetails, "")
        assertEquals(aiDisclosure1.otherAiDetails, "")
    }

    @Test
    fun testUserTransformer() {
        val userFragment = mock(User::class.java)
        `when`(userFragment.id).thenReturn("VXNlci0yMzc5NjEyNDM=")
        `when`(userFragment.name).thenReturn("Brotherwise Games")
        `when`(userFragment.imageUrl).thenReturn("https://ksr-qa-ugc.imgix.net/assets/005/791/327/f120c4cfe49495849b526b2cc6da44f9_original.png")
        `when`(userFragment.isCreator).thenReturn(true)
        `when`(userFragment.chosenCurrency).thenReturn(null)

        val user = userTransformer(userFragment)
        assertTrue(user.id() == 237961243L)
        assertTrue(user.name() == "Brotherwise Games")
        assertTrue(user.avatar().medium() == "https://ksr-qa-ugc.imgix.net/assets/005/791/327/f120c4cfe49495849b526b2cc6da44f9_original.png")
    }

    @Test
    fun testUserPrivacyTransformer() {
        val userPrivacyQuery = mock(UserPrivacyQuery.Me::class.java)
        `when`(userPrivacyQuery.name).thenReturn("Brotherwise Games")
        `when`(userPrivacyQuery.email).thenReturn("hello@kickstarter.com")
        `when`(userPrivacyQuery.hasPassword).thenReturn(true)
        `when`(userPrivacyQuery.isCreator).thenReturn(true)
        `when`(userPrivacyQuery.isDeliverable).thenReturn(true)
        `when`(userPrivacyQuery.isEmailVerified).thenReturn(true)
        `when`(userPrivacyQuery.chosenCurrency).thenReturn(null)

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
        val post = mock(com.kickstarter.fragment.Post::class.java)
        `when`(post.id).thenReturn("VXNlci0yMzc5NjEyNDM=")
        `when`(post.title).thenReturn("Updated Add-on list")
        `when`(post.isPublic).thenReturn(true)
        `when`(post.isVisible).thenReturn(true)
        `when`(post.isLiked).thenReturn(true)
        `when`(post.number).thenReturn(5)

        val date = DateTime.now().plusMillis(300)
        `when`(post.publishedAt).thenReturn(date)
        `when`(post.updatedAt).thenReturn(date)

        val user = updateTransformer(post)
        assertTrue(user.id() == 237961243L)
        assertTrue(user.projectId() == -1L)
        assertTrue(user.title() == "Updated Add-on list")
        assertTrue(user.isPublic() == true)
        assertTrue(user.visible() == true)
        assertTrue(user.hasLiked() == true)
        assertTrue(user.sequence() == 5)
        assertTrue(post.publishedAt == date)
        assertTrue(post.updatedAt == date)
    }

    @Test
    fun `test simpleShippingRuleTransformer provides appropriate shippingRule`() {
        val canadaSimpleSR = FetchProjectRewardsQuery.SimpleShippingRulesExpanded(
            "17.34562379823645234875620384756203847234",
            "CA",
            null,
            null,
            "TG9jYXRpb24tMjM0MjQ3NzU=",
            "Canada"
        )

        val australiaSR = FetchProjectRewardsQuery.SimpleShippingRulesExpanded(
            "0",
            "AU",
            "20", // - Shipping Rule disable at shipping, cost = 0, estimated range max/min values not null
            "2",
            "TG9jYXRpb24tMjM0MjQ3NDg=",
            "Australia"
        )

        val forbiddenValues = FetchProjectRewardsQuery.SimpleShippingRulesExpanded(
            "Pikachusito",
            "AU",
            "AyOma", // - Shipping Rule disable at shipping, cost = 0, estimated range max/min values not null
            "",
            "TG9jYXRpb24tMjM0MjQ3NDg=",
            "Australia"
        )

        val cadShipping = simpleShippingRuleTransformer(canadaSimpleSR)
        val ausShipping = simpleShippingRuleTransformer(australiaSR)

        // - Expected output model in case of forbidden values (non-numeric) on Cost/Monetary related fields
        val nonDoubleValues = simpleShippingRuleTransformer(forbiddenValues)

        assert(cadShipping.location()?.id() == decodeRelayId(canadaSimpleSR.locationId))
        assert(cadShipping.cost() == 17.345623798236453) // -  Rounds up after 15th digit
        assert(cadShipping.estimatedMax() == 0.0)
        assert(cadShipping.estimatedMin() == 0.0)
        assert(cadShipping.location()?.name() == canadaSimpleSR.locationName)
        assert(cadShipping.location()?.displayableName() == canadaSimpleSR.locationName)
        assert(cadShipping.location()?.country() == canadaSimpleSR.country)

        assert(ausShipping.location()?.id() == decodeRelayId(australiaSR.locationId))
        assert(ausShipping.cost() == 0.0)
        assert(ausShipping.estimatedMax() == 20.0)
        assert(ausShipping.estimatedMin() == 2.0)
        assert(ausShipping.location()?.name() == australiaSR.locationName)
        assert(ausShipping.location()?.displayableName() == australiaSR.locationName)
        assert(ausShipping.location()?.country() == australiaSR.country)

        assert(nonDoubleValues.location()?.id() == decodeRelayId(forbiddenValues.locationId))
        assert(nonDoubleValues.cost() == 0.0)
        assert(nonDoubleValues.estimatedMax() == 0.0)
        assert(nonDoubleValues.estimatedMin() == 0.0)
        assert(nonDoubleValues.location()?.name() == forbiddenValues.locationName)
        assert(nonDoubleValues.location()?.displayableName() == forbiddenValues.locationName)
        assert(nonDoubleValues.location()?.country() == forbiddenValues.country)
    }

    /**
     * Reward{__typename=Reward, id=UmV3YXJkLTk2NTM3NzY=, name=Stormgate Supporter, ...
     * Mock of a rewardFragment received from GraphQL data types
     */
    val fragmentReward = Reward(
        "UmV3YXJkLTk2NTM3NzY",
        "Stormgate Supporter",
        452,
        "Some reward description here",
        DateTime.now().toDate(),
        true,
        audienceData = Reward.AudienceData(secret = false),
        Reward.Amount("Amount", Amount("20.0", null, null)),
        Reward.PledgeAmount(
            "PledgeAmount", Amount("10.0", null, null)
        ),
        Reward.LatePledgeAmount(
            "LatePledgeAmount", Amount("30.0", null, null)
        ),
        Reward.ConvertedAmount(
            "ConvertedAmount", Amount("30.0", null, null)
        ),
        ShippingPreference.unrestricted,
        3,
        3,
        3,
        DateTime.now().minusDays(50),
        DateTime.now().plusDays(50),
        RewardType.base,
        AllowedAddons(listOf(Reward.Node("UmV3YXJkLTk3MDA2NjA"))),
        null,
    )

    @Test
    fun `test rewardTransformer returns appropriate shippingRules field when querying for simpleShippingRulesExpanded`() {

        val canadaSimpleSR = FetchProjectRewardsQuery.SimpleShippingRulesExpanded(
            "17.34562379823645234875620384756203847234",
            "CA",
            null,
            null,
            "TG9jYXRpb24tMjM0MjQ3NzU=",
            "Canada"
        )

        val australiaSR = FetchProjectRewardsQuery.SimpleShippingRulesExpanded(
            "0",
            "AU",
            "20", // - Shipping Rule disable at shipping, cost = 0, estimated range max/min values not null
            "2",
            "TG9jYXRpb24tMjM0MjQ3NDg=",
            "Australia"
        )

        val reward = rewardTransformer(
            rewardGr = fragmentReward,
            simpleShippingRules = listOf(canadaSimpleSR, australiaSR)
        )

        assertTrue(reward.shippingRules()?.size == 2)
        assertTrue(reward.shippingRules()?.first()?.id() == decodeRelayId(canadaSimpleSR.locationId))
        assertTrue(reward.shippingRules()?.last()?.id() == decodeRelayId(australiaSR.locationId))
    }
}
