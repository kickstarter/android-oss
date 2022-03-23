package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.ShippingRule;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ShippingRulesEnvelope implements Parcelable {
  public abstract List<ShippingRule> shippingRules();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder shippingRules(List<ShippingRule> ___);
    public abstract ShippingRulesEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_ShippingRulesEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
