package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.HamburgerNavigationItem;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public final class HamburgerNavigationFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_view) LinearLayout filterView;
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @BindColor(R.color.hamburger_navigation_item_selected) int filterSelectedColor;
  protected @BindColor(R.color.transparent) int filterUnselectedColor;
  private HamburgerNavigationItem item;
  private Delegate delegate;

  public interface Delegate {
    void filterClicked(final @NonNull HamburgerNavigationFilterViewHolder viewHolder, final @NonNull HamburgerNavigationItem hamburgerNavigationItem);
  }

  public HamburgerNavigationFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    this.item = (HamburgerNavigationItem) datum;
    final Context context = view.getContext();

    filterTextView.setText(item.discoveryParams().filterString(context));
    filterTextView.setTextAppearance(context, item.selected() ? R.style.SubheadPrimaryMedium : R.style.SubheadPrimary);

    filterView.setBackgroundColor(item.selected() ? filterSelectedColor : filterUnselectedColor);
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    Timber.d("Text view click");
    delegate.filterClicked(this, item);
  }
}

