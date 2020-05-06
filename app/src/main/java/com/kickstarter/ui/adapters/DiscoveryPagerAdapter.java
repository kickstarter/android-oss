package com.kickstarter.ui.adapters;

import android.view.ViewGroup;

import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.ArgumentsKey;
import com.kickstarter.ui.fragments.DiscoveryFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import rx.Observable;

public final class DiscoveryPagerAdapter extends FragmentPagerAdapter {
  private final Delegate delegate;
  private List<DiscoveryFragment> fragments;
  private List<String> pageTitles;

  public interface Delegate {
    void discoveryPagerAdapterSetPrimaryPage(DiscoveryPagerAdapter adapter, int position);
  }

  public DiscoveryPagerAdapter(final @NonNull FragmentManager fragmentManager, final @NonNull List<DiscoveryFragment> fragments,
    final @NonNull List<String> pageTitles, final Delegate delegate) {
    super(fragmentManager);

    this.delegate = delegate;
    this.fragments = fragments;
    this.pageTitles = pageTitles;
  }

  @Override
  public void setPrimaryItem(final @NonNull ViewGroup container, final int position, final @NonNull Object object) {
    super.setPrimaryItem(container, position, object);
    this.delegate.discoveryPagerAdapterSetPrimaryPage(this, position);
  }

  @Override
  public @NonNull Object instantiateItem(final @NonNull ViewGroup container, final int position) {
    final DiscoveryFragment fragment = (DiscoveryFragment) super.instantiateItem(container, position);
    this.fragments.set(position, fragment);
    return fragment;
  }

  @Override
  public @NonNull Fragment getItem(final int position) {
    return this.fragments.get(position);
  }

  @Override
  public int getCount() {
    return DiscoveryParams.Sort.defaultSorts.length;
  }

  @Override
  public CharSequence getPageTitle(final int position) {
    return this.pageTitles.get(position);
  }

  /**
   * Passes along root categories to its fragment position to help fetch appropriate projects.
   */
  public void takeCategoriesForPosition(final @NonNull List<Category> categories, final int position) {
    Observable.from(this.fragments)
      .filter(DiscoveryFragment::isInstantiated)
      .filter(DiscoveryFragment::isAttached)
      .filter(frag -> {
        final int fragmentPosition = frag.getArguments().getInt(ArgumentsKey.DISCOVERY_SORT_POSITION);
        return fragmentPosition == position;
      })
      .subscribe(frag -> frag.takeCategories(categories));
  }

  /**
   * Take current params from activity and pass to the appropriate fragment.
   */
  public void takeParams(final @NonNull DiscoveryParams params) {
    Observable.from(this.fragments)
      .filter(DiscoveryFragment::isInstantiated)
      .filter(DiscoveryFragment::isAttached)
      .filter(frag -> {
        final int fragmentPosition = frag.getArguments().getInt(ArgumentsKey.DISCOVERY_SORT_POSITION);
        return DiscoveryUtils.positionFromSort(params.sort()) == fragmentPosition;
      })
      .subscribe(frag -> frag.updateParams(params));
  }

  /**
   * Call when the view model tells us to clear specific pages.
   */
  public void clearPages(final @NonNull List<Integer> pages) {
    Observable.from(this.fragments)
      .filter(DiscoveryFragment::isInstantiated)
      .filter(DiscoveryFragment::isAttached)
      .filter(frag -> {
        final int fragmentPosition = frag.getArguments().getInt(ArgumentsKey.DISCOVERY_SORT_POSITION);
        return pages.contains(fragmentPosition);
      })
      .subscribe(DiscoveryFragment::clearPage);
  }

  public void scrollToTop(final int position) {
    Observable.from(this.fragments)
      .filter(DiscoveryFragment::isInstantiated)
      .filter(DiscoveryFragment::isAttached)
      .filter(frag -> {
        final int fragmentPosition = frag.getArguments().getInt(ArgumentsKey.DISCOVERY_SORT_POSITION);
        return position == fragmentPosition;
      })
      .subscribe(DiscoveryFragment::scrollToTop);
  }
}
