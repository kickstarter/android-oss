package com.kickstarter.libs;

import android.os.Parcelable;

import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;

import java.math.RoundingMode;

import androidx.annotation.NonNull;
import auto.parcel.AutoParcel;
import type.CurrencyCode;

public final class KSCurrency {
  private final CurrentConfigType currentConfig;

  public KSCurrency(final @NonNull CurrentConfigType currentConfig) {
    this.currentConfig = currentConfig;
  }

  /**
   * Returns a currency string appropriate to the user's locale and location relative to a project.
   *
   * @param initialValue Value to display, local to the project's currency.
   * @param project      The project to use to look up currency information.
   */
  public @NonNull String format(final double initialValue, final @NonNull Project project, final @NonNull RoundingMode roundingMode) {
    return format(initialValue, project, true, roundingMode);
  }

  /**
   * Returns a currency string appropriate to the user's locale and location relative to a project.
   *
   * @param initialValue        Value to display, local to the project's currency.
   * @param project             The project to use to look up currency information.
   * @param excludeCurrencyCode If true, hide the currency code, even if that makes the returned value ambiguous.
   *                            This is used when space is constrained and the currency code can be determined elsewhere.
   */
  public @NonNull String format(final double initialValue, final @NonNull Project project,
    final boolean excludeCurrencyCode) {

    return format(initialValue, project, excludeCurrencyCode, RoundingMode.DOWN);
  }

  /**
   * Returns a currency string appropriate to the user's locale and location relative to a project.
   *
   * @param initialValue        Value to display, local to the project's currency.
   * @param project             The project to use to look up currency information.
   * @param excludeCurrencyCode If true, hide the currency code, even if that makes the returned value ambiguous.
   *                            This is used when space is constrained and the currency code can be determined elsewhere.
   */
  public @NonNull String format(final double initialValue, final @NonNull Project project,
    final boolean excludeCurrencyCode, final @NonNull RoundingMode roundingMode) {

    final float unconvertedValue = roundingMode == RoundingMode.UP ? (float) Math.ceil(initialValue) : (float) Math.floor(initialValue);
    final CurrencyOptions currencyOptions = currencyOptions(unconvertedValue, project, excludeCurrencyCode, true);

    final boolean showCurrencyCode = showCurrencyCode(currencyOptions, excludeCurrencyCode);

    final NumberOptions numberOptions = NumberOptions.builder()
      .currencyCode(showCurrencyCode ? currencyOptions.currencyCode() : "")
      .currencySymbol(currencyOptions.currencySymbol())
      .roundingMode(roundingMode)
      .build();

    return NumberUtils.format(currencyOptions.value(), numberOptions);
  }

  /**
   * Returns a currency string appropriate to the user's locale and preferred currency.
   *
   * @param initialValue Value to display, local to the project's currency.
   * @param project The project to use to look up currency information.
   * @param roundingMode This determines whether we should round the values down or up.
   */
  public String formatWithUserPreference(final double initialValue, final @NonNull Project project, final @NonNull RoundingMode roundingMode) {

    final float unconvertedValue = roundingMode == RoundingMode.UP ? (float) Math.ceil(initialValue) : (float) Math.floor(initialValue);
    final CurrencyOptions currencyOptions = currencyOptions(unconvertedValue, project, true, false);

    final NumberOptions numberOptions = NumberOptions.builder()
      .currencySymbol(currencyOptions.currencySymbol())
      .roundingMode(roundingMode)
      .build();

    return NumberUtils.format(currencyOptions.value(), numberOptions);
  }

  /**
   * Build {@link CurrencyOptions} based on the project and whether we would prefer to show USD. Even if USD is preferred,
   * we only show USD if the user is in the US.
   */
  private @NonNull CurrencyOptions currencyOptions(final float value, final @NonNull Project project, final boolean excludeCurrencyCode,
    final boolean useProjectCurrency) {
    final Float fxRate = project.fxRate();

    return CurrencyOptions.builder()
      .country(project.country())
      .currencyCode("")
      .currencySymbol(getSymbolForCurrency(project, excludeCurrencyCode, useProjectCurrency))
      .value(useProjectCurrency ? value : value * fxRate)
      .build();
  }

  private String getSymbolForCurrency(final @NonNull Project project, final boolean excludeCurrencyCode, final boolean useProjectCurrency) {
    final Config config = this.currentConfig.getConfig();

    final String country = project.country();
    final String currency = useProjectCurrency ? project.currency() : project.currentCurrency();
    final boolean projectInUS = "US".equals(project.country());
    final boolean userInUS = "US".equals(config.countryCode());
    final CurrencyCode code = CurrencyCode.safeValueOf(currency);

    if (userInUS && excludeCurrencyCode) {
      if (useProjectCurrency && projectInUS) {
        return "$";
      } else if (!useProjectCurrency) {
        return "$";
      }
    }

    if (code == CurrencyCode.AUD) {
      return "AU$ ";
    } else if (code == CurrencyCode.CAD) {
      return "CA$ ";
    } else if (code == CurrencyCode.CHF) {
      return "CHF ";
    } else if (code == CurrencyCode.DKK) {
      return "DKK ";
    } else if (code == CurrencyCode.EUR) {
      return "€";
    } else if (code == CurrencyCode.GBP) {
      return "£";
    } else if (code == CurrencyCode.HKD) {
      return "HK$ ";
    } else if (code == CurrencyCode.JPY) {
      return "¥";
    } else if (code == CurrencyCode.MXN) {
      return "MX$ ";
    } else if (code == CurrencyCode.NOK) {
      return "NOK ";
    } else if (code == CurrencyCode.NZD) {
      return "NZ$ ";
    } else if (code == CurrencyCode.SEK) {
      return "SEK ";
    } else if (code == CurrencyCode.SGD) {
      return "S$ ";
    } else if (code == CurrencyCode.USD) {
      return "US$ ";
    } else {
      return "US$ ";
    }
  }

  /**
   * Determines whether the currency code should be shown. If the currency is ambiguous (e.g. CAD and USD both use `$`),
   * we show the currency code if the user is not in the US, or the project is not in the US.
   */
  private boolean showCurrencyCode(final @NonNull CurrencyOptions currencyOptions, final boolean excludeCurrencyCode) {
    final Config config = this.currentConfig.getConfig();
    final boolean currencyIsDupe = config.currencyNeedsCode(currencyOptions.currencySymbol());

    if (!currencyIsDupe) {
      return true;
    }

    final boolean userIsUS = config.countryCode().equals("US");
    final boolean projectIsUS = currencyOptions.country().equals("US");

    if (excludeCurrencyCode && userIsUS && projectIsUS) {
      return false;
    }

    return true;
  }

  @AutoParcel
  public abstract static class CurrencyOptions implements Parcelable {
    public abstract String country();
    public abstract String currencyCode();
    public abstract String currencySymbol();
    public abstract float value();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder country(String __);
      public abstract Builder currencyCode(String __);
      public abstract Builder currencySymbol(String __);
      public abstract Builder value(float __);
      public abstract CurrencyOptions build();
    }

    public static Builder builder() {
      return new AutoParcel_KSCurrency_CurrencyOptions.Builder();
    }

    public abstract Builder toBuilder();
  }
}
