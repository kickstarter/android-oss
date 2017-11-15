package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Category;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;


public interface ThanksCategoryHolderViewModel {

  interface Inputs {
    /** Call to configure view model with a category.  */
    void configureWith(Category category);

    /** Call when the view has been clicked*/
    void categoryViewClicked();
  }

  interface Outputs {
    /** Emits the category's name to be displayed. */
    Observable<String> categoryName();

    /** Emits when we should notify the delegate of the category click. */
    Observable<Category> notifyDelegateOfCategoryClick();
  }

  final class ViewModel extends ActivityViewModel<ThanksCategoryViewHolder> implements Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.categoryName = this.category.map(Category::name);
      this.notifyDelegateOfCategoryClick = this.category.compose(takeWhen(this.categoryViewClicked));
    }

    private final PublishSubject<Category> category = PublishSubject.create();
    private final PublishSubject<Void> categoryViewClicked = PublishSubject.create();

    private final Observable<String> categoryName;
    private final Observable<Category> notifyDelegateOfCategoryClick;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(Category category) {
      this.category.onNext(category);
    }
    @Override public void categoryViewClicked() {
      this.categoryViewClicked.onNext(null);
    }

    @Override public @NonNull Observable<String> categoryName() {
      return this.categoryName;
    }
    @Override public @NonNull Observable<Category> notifyDelegateOfCategoryClick() {
      return this.notifyDelegateOfCategoryClick;
    }
  }
}
