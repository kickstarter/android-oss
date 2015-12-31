package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.DiscoveryUtils;

import auto.parcel.AutoParcel;
import butterknife.Bind;
import butterknife.ButterKnife;

public final class DiscoveryFilterDividerViewHolder extends KSViewHolder {
  protected @Bind(R.id.categories_text_view) TextView categoriesTextView;
  protected @Bind(R.id.horizontal_line_thin_view) View horizontalLineView;
  private Divider divider;

  public DiscoveryFilterDividerViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  public void onBind(final @NonNull Object datum) {
    divider = (Divider) datum;

    categoriesTextView.setTextColor(color());
    horizontalLineView.setBackgroundColor(color());
  }

  private int color() {
   return DiscoveryUtils.overlayTextColor(view.getContext(), divider.light());
  }

  @AutoParcel
  public abstract static class Divider {
    public abstract boolean light();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder light(boolean __);
      public abstract Divider build();
    }

    public static Builder builder() {
      return new AutoParcel_DiscoveryFilterDividerViewHolder_Divider.Builder();
    }
  }
}
