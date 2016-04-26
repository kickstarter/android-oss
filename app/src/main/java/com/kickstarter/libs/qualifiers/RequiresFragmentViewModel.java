package com.kickstarter.libs.qualifiers;

import com.kickstarter.libs.FragmentViewModel;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresFragmentViewModel {
  Class<? extends FragmentViewModel> value();
}
