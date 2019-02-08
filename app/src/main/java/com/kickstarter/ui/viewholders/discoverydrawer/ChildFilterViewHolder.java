package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Category;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.KSViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ChildFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @BindColor(R.color.ksr_dark_grey_400) int ksrDarkGrayColor;
  protected @BindColor(R.color.accent) int accentColor;

  private final KSString ksString;

  private NavigationDrawerData.Section.Row item;
  private Delegate delegate;

  public interface Delegate {
    void childFilterViewHolderRowClick(final @NonNull ChildFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row);
  }

  public ChildFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.item = requireNonNull((NavigationDrawerData.Section.Row) data, NavigationDrawerData.Section.Row.class);
  }

  @Override
  public void onBind() {
    final Context context = context();

    final Category category = this.item.params().category();
    if (category != null && category.isRoot()) {
      this.filterTextView.setText(this.item.params().filterString(context, this.ksString));
    } else {
      this.filterTextView.setText(this.item.params().filterString(context, this.ksString));
    }

    final int textColor = this.item.selected() ? this.accentColor : this.ksrDarkGrayColor;
    this.filterTextView.setTextColor(textColor);

    final Drawable drawable = this.item.selected() ? ContextCompat.getDrawable(context, R.drawable.drawer_selected) : null;
    this.filterTextView.setBackground(drawable);
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    Timber.d("DiscoveryDrawerChildParamsViewHolder topFilterViewHolderRowClick");
    this.delegate.childFilterViewHolderRowClick(this, this.item);
  }
}

