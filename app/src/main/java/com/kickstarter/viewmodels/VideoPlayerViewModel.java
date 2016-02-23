package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.VideoPlayerActivity;
import com.kickstarter.viewmodels.outputs.VideoPlayerViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class VideoPlayerViewModel extends ViewModel<VideoPlayerActivity> implements VideoPlayerViewModelOutputs {

  private final BehaviorSubject<Video> video = BehaviorSubject.create();
  @Override
  public Observable<Video> video() {
    return video;
  }

  public final VideoPlayerViewModel outputs = this;

  public VideoPlayerViewModel(final @NonNull Environment environment) {
    super(environment);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class)
      .filter(ObjectUtils::isNotNull)
      .map(Project::video)
      .filter(ObjectUtils::isNotNull)
      .take(1)
      .subscribe(video::onNext);
  }
}
