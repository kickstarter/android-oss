package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.kickstarter.libs.utils.ObjectUtils.*;

public final class HamburgerNavigationChildFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @BindColor(R.color.black) int blackColor;
  protected @BindColor(R.color.dark_gray) int darkGrayColor;

  protected @Inject KSString ksString;

  private NavigationDrawerData.Section.Row item;
  private Delegate delegate;

  public interface Delegate {
    void rowClick(final @NonNull HamburgerNavigationChildFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row);
  }

  public HamburgerNavigationChildFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    item = requireNonNull((NavigationDrawerData.Section.Row) data, NavigationDrawerData.Section.Row.class);
  }

  @Override
  public void onBind() {
    final Context context = view.getContext();

    final Category category = item.params().category();
    if (category != null && category.isRoot()) {
      filterTextView.setText(ksString.format(context.getString(R.string.discovery_filters_all_of_category), "category_name", item.params().filterString(context)));
    } else {
      filterTextView.setText(item.params().filterString(context));
    }
    if (item.selected()) {
      filterTextView.setTextAppearance(context, R.style.SubheadPrimaryMedium);
      filterTextView.setTextColor(blackColor);
    } else {
      filterTextView.setTextAppearance(context, R.style.SubheadPrimary);
      filterTextView.setTextColor(darkGrayColor);
    }
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    Timber.d("HamburgerNavigationChildFilterViewHolder rowClick");
    delegate.rowClick(this, item);
  }
}

