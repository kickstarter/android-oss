package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Category;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.DiscoveryFilterActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class DiscoveryFilterPresenter extends Presenter<DiscoveryFilterActivity> {
  @Inject ApiClient apiClient;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Category>> categories = apiClient.fetchCategories();

    RxUtils.combineLatestPair(viewChange, categories)
      .filter(vc -> vc.first != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vc -> vc.first.onItemsNext(vc.second));
  }
}
