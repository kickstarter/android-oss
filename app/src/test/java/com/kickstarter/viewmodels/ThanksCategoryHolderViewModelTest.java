package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.mock.factories.CategoryFactory;
import com.kickstarter.models.Category;
import com.kickstarter.ui.viewholders.ThanksCategoryHolderViewModel;

import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public final class ThanksCategoryHolderViewModelTest extends KSRobolectricTestCase {
  private ThanksCategoryHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> categoryName = new TestSubscriber<>();
  private final TestSubscriber<Category> notifyDelegateOfCategoryClick = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new ThanksCategoryHolderViewModel.ViewModel(environment);
    this.vm.getOutputs().categoryName().subscribe(this.categoryName);
    this.vm.getOutputs().notifyDelegateOfCategoryClick().subscribe(this.notifyDelegateOfCategoryClick);
  }

  @Test
  public void testCategoryName() {
    final Category category = CategoryFactory.musicCategory();
    setUpEnvironment(environment());

    this.vm.getInputs().configureWith(category);
    this.categoryName.assertValues(category.name());
  }

  @Test
  public void testCategoryViewClicked() {
    final Category category = CategoryFactory.bluesCategory();
    setUpEnvironment(environment());

    this.vm.getInputs().configureWith(category);
    this.vm.getInputs().categoryViewClicked();
    this.notifyDelegateOfCategoryClick.assertValues(category);
  }
}
