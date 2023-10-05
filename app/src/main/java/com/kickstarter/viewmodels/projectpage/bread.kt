package com.kickstarter.viewmodels.projectpage

import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException

inline fun <T> Observable<T>.dropBreadcrumb(): Observable<T> {
    val breadcrumb = BreadcrumbException()
    return this.onErrorResumeNext { error: Throwable ->
        throw CompositeException(error, breadcrumb)
    }
}

inline fun <T> rx.Observable<T>.dropBreadcrumb(): rx.Observable<T> {
    val breadcrumb = BreadcrumbException()
    return this.onErrorResumeNext { error: Throwable ->
        throw rx.exceptions.CompositeException(error, breadcrumb)
    }
}

class BreadcrumbException : Exception()