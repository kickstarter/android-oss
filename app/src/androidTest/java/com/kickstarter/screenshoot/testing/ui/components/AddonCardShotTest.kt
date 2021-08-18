package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.R
import com.kickstarter.mock.factories.RewardsItemFactory
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.adapters.RewardItemsAdapter
import com.kickstarter.ui.views.AddOnCard
import com.kickstarter.ui.views.Stepper
import org.junit.Before
import org.junit.Test

class AddonCardShotTest : ScreenshotTest {

    private lateinit var addonCard: AddOnCard
    private lateinit var stepper: Stepper
    lateinit var component: ApplicationComponent
    var itemsAdapter: RewardItemsAdapter = RewardItemsAdapter()

    @Before
    fun setup() {
        // - Test Application
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()
    }

    @Test
    fun descriptionInvisible() {
        addonCard = (LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
            R.layout.item_add_on_pledge, null) as CardView)
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(false)
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun descriptionVisible() {
        addonCard = (LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
            R.layout.item_add_on_pledge, null) as CardView)
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        addonCard.setUpItemsAdapter(itemsAdapter, LinearLayoutManager(InstrumentationRegistry.getInstrumentation().targetContext))
        itemsAdapter.rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun descriptionInvisible() {
        addonCard = (LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
            R.layout.item_add_on_pledge, null) as CardView)
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(false)
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }

    @Test
    fun descriptionVisible() {
        addonCard = (LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
            R.layout.item_add_on_pledge, null) as CardView)
            .findViewById(R.id.add_on_card)

        stepper = addonCard.findViewById(R.id.add_on_stepper) as Stepper

        addonCard.setAddonDescriptionVisibility(true)
        addonCard.setAddOnDescription("Description")
        addonCard.setAddOnItemLayoutVisibility(true)
        addonCard.setDividerVisibility(true)
        addonCard.setAddOnTitleText("Title Text")
        addonCard.setAddOnMinimumText("5")
        addonCard.setAddonConversionVisibility(true)
        addonCard.setAddonConversionText("Conversion")
        addonCard.setBackerLimitPillVisibility(true)
        addonCard.setBackerLimitText("Backer limit")
        addonCard.setAddonQuantityRemainingPillVisibility(true)
        addonCard.setAddonQuantityRemainingText("Quantity remaining")
        addonCard.setTimeLeftVisibility(true)
        addonCard.setTimeLeftText("Time left")
        addonCard.setShippingAmountText("Shipping amount")
        addonCard.setShippingAmountVisibility(true)
        stepper.inputs.setInitialValue(0)
        stepper.inputs.setMinimum(0)
        stepper.inputs.setMaximum(10)

        compareScreenshot(addonCard)
    }
}