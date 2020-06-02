package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Backing;
import com.kickstarter.models.BackingWrapper;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApolloClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.BackingActivity;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.neverError;

public interface BackingViewModel {

  interface Outputs {
    /** Set the backer name TextView's text. */
    Observable<BackingWrapper> showBackingFragment();
  }

  final class ViewModel extends ActivityViewModel<BackingActivity> implements Outputs {
    private final CurrentUserType currentUser;
    private ApolloClientType apolloClient;

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
        .compose(neverError())
        .share();

      Observable.combineLatest(backing, project,loggedInUser, this::createWrapper)
              .subscribe(this.backingWrapper::onNext);
    }

    public final Outputs outputs = this;

    private BackingWrapper createWrapper(Backing backing, Project project, User currentUser) {
      return new BackingWrapper(backing, currentUser, project);
    }

    @Override
    public Observable<BackingWrapper> showBackingFragment() {
      return this.backingWrapper;
    }
  }
}
