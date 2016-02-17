package com.kickstarter.libs.qualifiers;

import auto.parcel.AutoParcel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Adapted from: https://gist.github.com/JakeWharton/0d67d01badcee0ae7bc9

/**
 * Marks an {@link AutoParcel @AutoParcel}-annotated type for proper Gson serialization.
 * <p>
 * This annotation is needed because the {@linkplain Retention retention} of {@code @AutoParcel}
 * does not allow reflection at runtime.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoGson {
}
