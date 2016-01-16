package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.KSViewHolder;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class TopFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_view) LinearLayout filterView;
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @BindColor(R.color.hamburger_navigation_item_selected) int filterSelectedColor;
  protected @BindColor(R.color.transparent) int filterUnselectedColor;
  private NavigationDrawerData.Section.Row item;
  private Delegate delegate;

  public interface Delegate {
    void rowClick(final @NonNull TopFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row);
  }

  public TopFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    item = requireNonNull((NavigationDrawerData.Section.Row) data, NavigationDrawerData.Section.Row.class);
  }

  @Override
  public void onBind() {
    final Context context = view.getContext();

    filterTextView.setText(item.params().filterString(context));
    filterTextView.setTextAppearance(context, item.selected() ? R.style.SubheadPrimaryMedium : R.style.SubheadPrimary);

    filterView.setBackgroundColor(item.selected() ? filterSelectedColor : filterUnselectedColor);
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    delegate.rowClick(this, item);
  }
}

