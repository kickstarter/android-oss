package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.ThanksActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class ThanksPresenter extends Presenter<ThanksActivity> {
  private final PublishSubject<Void> shareMoreClick = PublishSubject.create();
  private final PublishSubject<Void> doneClick = PublishSubject.create();

  public void takeProject(final Project project) {
    final Observable<Project> p = Observable.just(project);

    final Observable<Project> projectShareClick =

    final Observable<Pair<Project, ThanksActivity>> projectAndActivity = RxUtils.combineLatestPair(p, viewSubject)
        .filter(pair -> pair.second != null);


    // TODO: Load recommendations

    addSubscription(RxUtils.combineLatestPair(p, viewSubject)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pair -> pair.second.show(pair.first)));

    addSubscription(doneClick
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pair -> done(pair.first)));

    addSubscription(shareMoreClick
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pair -> share(pair.first.second, pair.first.first)));
  }

  public void takeDoneClick() {
    doneClick.onNext(null);
  }

  public void takeShareMoreClick() {
    Timber.d("takeShareMoreClick");
    shareMoreClick.onNext(null);
  }

  private void share(final Context context, final Project project) {
    Timber.d("Share intent");
    final Intent intent = new Intent(android.content.Intent.ACTION_SEND)
      .setType("text/plain")
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
      .putExtra(Intent.EXTRA_TEXT, context.getResources()
        .getString(R.string.I_just_backed_project_on_Kickstarter, project.name(), project
          .secureWebProjectUrl()));

    context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.Share_this_project)));
  }

  private void done(final Context context) {
    Timber.d("Done intent");
    final Intent intent = new Intent(context, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    context.startActivity(intent);
  }
}
