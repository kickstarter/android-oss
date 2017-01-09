package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  /**
   * Emits a boolean that determines if the drawer is open or not.
   */
  Observable<Boolean> drawerIsOpen();

  /**
   * Emits a booelan that determines if the sort tab layout should be expanded/collapsed.
   */
  Observable<Boolean> expandSortTabLayout();

  /**
   * Emits when params change so that the tool bar can adjust accordingly.
   */
  Observable<DiscoveryParams> updateToolbarWithParams();

  /**
   * Emits when the params of a particular page should be updated. The page will be responsible for
   * taking those params and creating paginating projects from it.
   */
  Observable<DiscoveryParams> updateParamsForPage();

  Observable<NavigationDrawerData> navigationDrawerData();

  /**
   * Emits the root categories and position. Position is used to determine the appropriate fragment
   * to pass the categories to.
   */
  Observable<Pair<List<Category>, Integer>> rootCategoriesAndPosition();

  /**
   * Emits a list of pages that should be cleared of all their content.
   */
  Observable<List<Integer>> clearPages();

  /**
   * Emits when a newer build is available and an alert should be shown.
   */
  Observable<InternalBuildEnvelope> showBuildCheckAlert();

  Observable<Void> showLoginTout();
  Observable<Void> showProfile();
  Observable<Void> showSettings();
  Observable<Void> showInternalTools();
}
