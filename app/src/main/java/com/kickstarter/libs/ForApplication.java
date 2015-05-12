package com.kickstarter.libs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/*
 * Allows the application context to be injected but requires a ForApplication
 * annotation, checked at compile time.
 *
 * This makes it explicit that we are injecting an application context, rather than
 * a generic context or specific context (e.g. activity context).
*/
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ForApplication {
}
