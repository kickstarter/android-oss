package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Category;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.KSViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ChildFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_view) LinearLayout filterView;
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @BindColor(R.color.ksr_soft_black) int ksrSoftBlackColor;
  protected @BindColor(R.color.ksr_dark_grey_400) int darkGrayColor;
  protected @BindColor(R.color.discovery_drawer_item_selected) int filterSelectedColor;
  protected @BindColor(R.color.transparent) int filterUnselectedColor;

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
    if (this.item.selected()) {
      this.filterTextView.setTextAppearance(context, R.style.CalloutPrimaryMedium);
      this.filterTextView.setTextColor(this.ksrSoftBlackColor);
    } else {
      this.filterTextView.setTextAppearance(context, R.style.CalloutPrimary);
      this.filterTextView.setTextColor(this.darkGrayColor);
    }

    this.filterView.setBackgroundColor(this.item.selected() ? this.filterSelectedColor : this.filterUnselectedColor);
  }

  @OnClick(R.id.filter_text_view)
  protected void textViewClick() {
    Timber.d("DiscoveryDrawerChildParamsViewHolder topFilterViewHolderRowClick");
    this.delegate.childFilterViewHolderRowClick(this, this.item);
  }
}

