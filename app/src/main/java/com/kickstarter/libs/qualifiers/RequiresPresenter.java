package com.kickstarter.libs.qualifiers;

import com.kickstarter.libs.Presenter;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPresenter {
  Class<? extends Presenter> value();
}
