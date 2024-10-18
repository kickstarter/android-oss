package com.kickstarter.ui.viewholders

import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.models.Category
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface ThanksCategoryHolderViewModel {

    interface Inputs {
        /** Call to configure view model with a category.   */
        fun configureWith(category: Category)

        /** Call when the view has been clicked */
        fun categoryViewClicked()
    }

    interface Outputs {
        /** Emits the category's name to be displayed.  */
        fun categoryName(): Observable<String>

        /** Emits when we should notify the delegate of the category click.  */
        fun notifyDelegateOfCategoryClick(): Observable<Category>
    }

    class ViewModel(val environment: Environment) : Inputs, Outputs {
        private val category = PublishSubject.create<Category>()
        private val categoryViewClicked = PublishSubject.create<Unit>()

        private val categoryName: Observable<String>
        private val notifyDelegateOfCategoryClick: Observable<Category>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.categoryName = this.category.map { it.name() }
            this.notifyDelegateOfCategoryClick = this.category.compose(takeWhenV2(this.categoryViewClicked))
        }

        override fun configureWith(category: Category) {
            this.category.onNext(category)
        }
        override fun categoryViewClicked() {
            this.categoryViewClicked.onNext(Unit)
        }

        override fun categoryName(): Observable<String> {
            return this.categoryName
        }
        override fun notifyDelegateOfCategoryClick(): Observable<Category> {
            return this.notifyDelegateOfCategoryClick
        }
    }
}
