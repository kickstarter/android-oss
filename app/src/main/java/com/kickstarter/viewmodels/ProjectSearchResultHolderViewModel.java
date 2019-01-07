package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface ProjectSearchResultHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a project and isFeatured data. */
    void configureWith(Pair<Project, Boolean> projectAndIsFeatured);

    /** Call to say user clicked a project */
    void projectClicked();
  }

  interface Outputs {
    /** Emits the formatted days to go text to be displayed. */
    Observable<String> deadlineCountdownValueTextViewText();

    /** Emits the project clicked by the user. */
    Observable<Project> notifyDelegateOfResultClick();

    /** Emits the percent funded text to be displayed. */
    Observable<String> percentFundedTextViewText();

    /** Emits the project be used to display the deadline countdown detail. */
    Observable<Project> projectForDeadlineCountdownUnitTextView();

    /** Emits the project title to be displayed. */
    Observable<String> projectNameTextViewText();

    /** Emits the project photo url to be displayed. */
    Observable<String> projectPhotoUrl();
  }

  final class ViewModel extends ActivityViewModel<ProjectSearchResultViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.deadlineCountdownValueTextViewText = this.projectAndIsFeatured
        .map(pb -> NumberUtils.format(ProjectUtils.deadlineCountdownValue(pb.first)));

      this.percentFundedTextViewText = this.projectAndIsFeatured
        .map(pb -> NumberUtils.flooredPercentage(pb.first.percentageFunded()));

      this.projectForDeadlineCountdownDetail = this.projectAndIsFeatured
        .map(pb -> pb.first);

      this.projectPhotoUrl = this.projectAndIsFeatured
        .map(pb -> Pair.create(pb.first.photo(), pb.second))
        .filter(pb -> ObjectUtils.isNotNull(pb.first))
        .map(pb -> pb.second ? pb.first.full() : pb.first.med());

      this.projectNameTextViewText = this.projectAndIsFeatured
        .map(pb -> pb.first.name());

      this.notifyDelegateOfResultClick = this.projectAndIsFeatured
        .map(PairUtils::first)
        .compose(takeWhen(this.projectClicked));
    }

    private final PublishSubject<Pair<Project, Boolean>> projectAndIsFeatured = PublishSubject.create();
    private final PublishSubject<Void> projectClicked = PublishSubject.create();

    private final Observable<String> deadlineCountdownValueTextViewText;
    private final Observable<Project> notifyDelegateOfResultClick;
    private final Observable<String> percentFundedTextViewText;
    private final Observable<Project> projectForDeadlineCountdownDetail;
    private final Observable<String> projectNameTextViewText;
    private final Observable<String> projectPhotoUrl;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull Pair<Project, Boolean> projectAndIsFeatured) {
      this.projectAndIsFeatured.onNext(projectAndIsFeatured);
    }
    @Override public void projectClicked() {
      this.projectClicked.onNext(null);
    }

    @Override public @NonNull Observable<String> deadlineCountdownValueTextViewText() {
      return this.deadlineCountdownValueTextViewText;
    }
    @Override public @NonNull Observable<Project> notifyDelegateOfResultClick() {
      return this.notifyDelegateOfResultClick;
    }
    @Override public @NonNull Observable<String> percentFundedTextViewText() {
      return this.percentFundedTextViewText;
    }
    @Override public @NonNull Observable<Project> projectForDeadlineCountdownUnitTextView() {
      return this.projectForDeadlineCountdownDetail;
    }
    @Override public @NonNull Observable<String> projectPhotoUrl() {
      return this.projectPhotoUrl;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
  }
}
