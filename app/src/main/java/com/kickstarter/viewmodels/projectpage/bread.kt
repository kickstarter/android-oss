package com.kickstarter.viewmodels.projectpage

import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException

inline fun <T> Observable<T>.dropBreadcrumb(): Observable<T> {
    val breadcrumb = BreadcrumbException()
    return this.onErrorResumeNext { error: Throwable ->
        throw CompositeException(error, breadcrumb)
    }
}

class BreadcrumbException : Exception()