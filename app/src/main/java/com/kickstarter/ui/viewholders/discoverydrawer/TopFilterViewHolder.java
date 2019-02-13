package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.KSViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class TopFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @BindColor(R.color.accent) int accentColor;
  protected @BindColor(R.color.ksr_dark_grey_500) int ksrDarkGrayColor;
  protected @BindColor(R.color.ksr_soft_black) int ksrSoftBlackColor;
  protected @BindDrawable(R.drawable.ic_label_green) Drawable labelSelectedDrawable;
  protected @BindDrawable(R.drawable.ic_label) Drawable labelUnselectedDrawable;
  protected @BindDrawable(R.drawable.drawer_selected) Drawable selectedBackgroundDrawable;
  private NavigationDrawerData.Section.Row item;
  private Delegate delegate;

  public interface Delegate {
    void topFilterViewHolderRowClick(final @NonNull TopFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row);
  }

  public TopFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
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

    this.filterTextView.setText(this.item.params().filterString(context, environment().ksString()));

    final int textColor = this.item.selected() ? this.accentColor : this.ksrSoftBlackColor;
    this.filterTextView.setTextColor(textColor);

    final Drawable iconDrawable = this.item.selected() ? this.labelSelectedDrawable : this.labelUnselectedDrawable;
    this.filterTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(iconDrawable, null, null, null);

    final Drawable backgroundDrawable = this.item.selected() ? this.selectedBackgroundDrawable : null;
    this.filterTextView.setBackground(backgroundDrawable);
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    this.delegate.topFilterViewHolderRowClick(this, this.item);
  }
}

