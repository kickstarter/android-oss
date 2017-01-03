package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.views.IconButton;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ParentFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @Bind(R.id.expand_button) IconButton expandButton;
  protected @Bind(R.id.collapse_button) IconButton collapseButton;
  private NavigationDrawerData.Section.Row item;
  private Delegate delegate;

  protected @Inject KSString ksString;

  public interface Delegate {
    void parentFilterViewHolderRowClick(final @NonNull ParentFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row);
  }

  public ParentFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
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
    final Context context = context();

    filterTextView.setText(item.params().filterString(context, ksString, false, true));

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
    delegate.parentFilterViewHolderRowClick(this, item);
  }
}

