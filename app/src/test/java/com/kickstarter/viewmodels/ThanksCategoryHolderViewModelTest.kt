package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.CategoryFactory.musicCategory
import com.kickstarter.models.Category
import com.kickstarter.ui.viewholders.ThanksCategoryHolderViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ThanksCategoryHolderViewModelTest : KSRobolectricTestCase() {
    private var vm: ThanksCategoryHolderViewModel.ViewModel? = null
    private val categoryName = TestSubscriber<String>()
    private val notifyDelegateOfCategoryClick = TestSubscriber<Category>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ThanksCategoryHolderViewModel.ViewModel(environment)
        vm!!.outputs.categoryName().subscribe { this.categoryName.onNext(it) }.addToDisposable(disposables)
        vm!!.outputs.notifyDelegateOfCategoryClick().subscribe { this.notifyDelegateOfCategoryClick.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testCategoryName() {
        val category = musicCategory()
        setUpEnvironment(environment())

        vm!!.inputs.configureWith(category)
        categoryName.assertValues(category.name())
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
