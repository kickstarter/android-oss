package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.ui.adapters.NavigationDrawerAdapter;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public final class HamburgerNavigationRootFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  private NavigationDrawerAdapter.Data.Section.Row item;
  private Delegate delegate;

  public interface Delegate {
    void rowClick(final @NonNull HamburgerNavigationRootFilterViewHolder viewHolder, final @NonNull NavigationDrawerAdapter.Data.Section.Row row);
  }

  public HamburgerNavigationRootFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    this.item = (NavigationDrawerAdapter.Data.Section.Row) datum;
    final Context context = view.getContext();

    filterTextView.setText(item.params().filterString(context));
    filterTextView.setTextAppearance(context, item.selected() ? R.style.SubheadPrimaryMedium : R.style.SubheadPrimary);
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    Timber.d("HamburgerNavigationRootFilterViewHolder rowClick");
    delegate.rowClick(this, item);
  }
}

