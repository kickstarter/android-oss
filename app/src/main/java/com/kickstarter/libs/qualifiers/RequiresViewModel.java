package com.kickstarter.libs.qualifiers;

import com.kickstarter.libs.ViewModel;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresViewModel {
  Class<? extends ViewModel> value();
}
