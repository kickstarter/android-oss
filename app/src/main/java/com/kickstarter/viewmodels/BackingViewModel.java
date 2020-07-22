package com.kickstarter.viewmodels;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.BackingWrapper;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApolloClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.BackingActivity;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface BackingViewModel {

  interface Inputs {
    void refresh();
  }

  interface Outputs {
    /**
     * Set the backer name TextView's text.
     */
    Observable<BackingWrapper> showBackingFragment();

    Observable<Boolean> isRefreshing();
  }

  final class ViewModel extends ActivityViewModel<BackingActivity> implements Outputs, Inputs {
    private final CurrentUserType currentUser;
    private ApolloClientType apolloClient;
    private PublishSubject<Void> refreshBacking = PublishSubject.create();
    private PublishSubject<Boolean> isRefreshing = PublishSubject.create();


    private final PublishSubject<BackingWrapper> backingWrapper = PublishSubject.create();

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.currentUser = environment.currentUser();
      this.apolloClient = environment.apolloClient();

      final Observable<User> loggedInUser = this.currentUser.loggedInUser();

      final Observable<Project> project = intent()
              .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
              .ofType(Project.class);

      final Observable<Backing> backingInfo = intent()
              .map(i -> i.getParcelableExtra(IntentKey.BACKING));

      final Observable<Backing> backing = Observable.combineLatest(project, backingInfo, Pair::create)
              .switchMap(pb -> this.apolloClient.getBacking(Long.toString(pb.second.id())))
              .distinctUntilChanged()
              .filter(bk -> ObjectUtils.isNotNull(bk))
              .compose(neverError());

      backing.compose(takeWhen(refreshBacking))
              .switchMap(backing1 -> this.apolloClient.getBacking(Long.toString(backing1.id())))
              .filter(ObjectUtils::isNotNull)
              .subscribe(it -> this.isRefreshing.onNext(false));

      Observable.combineLatest(backing, project, loggedInUser, this::createWrapper)
              .subscribe(this.backingWrapper::onNext);


    }

    public final Outputs outputs = this;
    public final Inputs inputs = this;

    private BackingWrapper createWrapper(final Backing backing, final Project project, final User currentUser) {
      return new BackingWrapper(backing, currentUser, project);
    }

    @Override
    public Observable<BackingWrapper> showBackingFragment() {
      return this.backingWrapper;
    }

    @Override
    public void refresh() {
      this.refreshBacking.onNext(null);
    }

    @Override
    public Observable<Boolean> isRefreshing() {
      return this.isRefreshing;
    }
  }
}
