package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.views.IconButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

  public interface Delegate {
    void parentFilterViewHolderRowClick(final @NonNull ParentFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row);
  }

  public ParentFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.item = requireNonNull((NavigationDrawerData.Section.Row) data, NavigationDrawerData.Section.Row.class);
  }

  @Override
  public void onBind() {
    final Context context = context();

    this.filterTextView.setText(this.item.params().filterString(context, environment().ksString(), false, true));

    if (this.item.rootIsExpanded()) {
      this.expandButton.setVisibility(View.GONE);
      this.collapseButton.setVisibility(View.VISIBLE);
    } else {
      this.expandButton.setVisibility(View.VISIBLE);
      this.collapseButton.setVisibility(View.GONE);
    }
  }

  @OnClick(R.id.filter_view)
  protected void rowClick() {
    this.delegate.parentFilterViewHolderRowClick(this, this.item);
  }
}

