package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.views.IconButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.kickstarter.libs.utils.ObjectUtils.*;

public final class HamburgerNavigationRootFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @Bind(R.id.expand_button) IconButton expandButton;
  protected @Bind(R.id.collapse_button) IconButton collapseButton;
  private NavigationDrawerData.Section.Row item;
  private Delegate delegate;

  public interface Delegate {
    void rowClick(final @NonNull HamburgerNavigationRootFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row);
  }

  public HamburgerNavigationRootFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
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

    if (item.rootIsExpanded()) {
      expandButton.setVisibility(View.GONE);
      collapseButton.setVisibility(View.VISIBLE);
    } else {
      expandButton.setVisibility(View.VISIBLE);
      collapseButton.setVisibility(View.GONE);
    }
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    Timber.d("HamburgerNavigationRootFilterViewHolder rowClick");
    delegate.rowClick(this, item);
  }
}

