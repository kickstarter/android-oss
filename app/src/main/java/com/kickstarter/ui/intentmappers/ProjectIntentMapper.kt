package com.kickstarter.ui.intentmappers

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.query
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.IntentKey
import java.util.regex.Pattern

object ProjectIntentMapper {
    const val SCHEME_KSR = "ksr"
    const val SCHEME_HTTPS = "https"

    // /projects/param-1/param-2*
    val PROJECT_PATTERN =
        Pattern.compile("\\A\\/projects\\/([a-zA-Z0-9_-]+)(\\/([a-zA-Z0-9_-]+)).*")

    private val PROJECT_SAVE_QUERY_PATTERN = Pattern.compile(
        "save(\\=[a-zA-Z]+)"
    )

    fun project(intent: Intent, apolloClient: ApolloClientTypeV2): io.reactivex.Observable<Project> {
        val intentProject = projectFromIntent(intent)
        val projectFromParceledProject =
            if (intentProject == null) io.reactivex.Observable.empty() else io.reactivex.Observable.just(intentProject)
                .switchMap { project: Project? ->
                    project?.let { apolloClient.getProject(it) }
                }
                .startWith(intentProject)
                .retry(3)

        val projectFromParceledParam = io.reactivex.Observable.just(paramFromIntent(intent) ?: "")
            .filter { it.isNotEmpty() }
            .switchMap { slug: String? ->
                slug?.let { apolloClient.getProject(it) }
            }
            .retry(3)
        return projectFromParceledProject
            .mergeWith(projectFromParceledParam)
    }

    /**
     * Returns an observable of projects retrieved from intent data. May hit the API if the intent only contains a project
     * param rather than a parceled project.
     */
    fun project(intent: Intent, client: ApiClientTypeV2): io.reactivex.Observable<Project> {
        val intentProject = projectFromIntent(intent)
        val projectFromParceledProject =
            if (intentProject == null) io.reactivex.Observable.empty() else io.reactivex.Observable.just(intentProject)
                .flatMap { project: Project? ->
                    project?.let { client.fetchProject(it) }
                }
                .startWith(intentProject)
                .retry(3)

        val projectFromParceledParam = io.reactivex.Observable.just(paramFromIntent(intent) ?: "")
            .filter { it.isNotEmpty() }
            .flatMap { param: String ->
                client.fetchProject(param)
            }
            .retry(3)
        return projectFromParceledProject
            .mergeWith(projectFromParceledParam)
    }

    /**
     * Returns a [RefTag] observable. If there is no parceled RefTag, emit empty optionl.
     */
    fun refTag(intent: Intent): io.reactivex.Observable<KsOptional<RefTag?>> {
        return io.reactivex.Observable.just(KsOptional.of(intent.getParcelableExtra(IntentKey.REF_TAG)))
    }

    /**
     * Returns a [deepLinkSaveFlag] observable. If there is no deepLink Save Flag
     */
    fun deepLinkSaveFlag(intent: Intent): io.reactivex.Observable<Boolean> {
        return io.reactivex.Observable.just(intent.getBooleanExtra(IntentKey.SAVE_FLAG_VALUE, false))
    }

    /**
     * Gets a parceled project from the intent data, may return `null`.
     */
    private fun projectFromIntent(intent: Intent): Project? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(IntentKey.PROJECT, Project::class.java)
        } else {
            intent.getParcelableExtra(IntentKey.PROJECT) as? Project?
        }
    }

    /**
     * Gets a project param from the intent data, may return `null`.
     */
    private fun paramFromIntent(intent: Intent): String? {
        return if (intent.hasExtra(IntentKey.PROJECT_PARAM)) {
            intent.getStringExtra(IntentKey.PROJECT_PARAM)
        } else paramFromUri(IntentMapper.uri(intent))
    }

    /**
     * Extract the project param from a uri. e.g.: A uri like `ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee`
     * returns `skull-graphic-tee`.
     */
    fun paramFromUri(uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        val scheme = uri.scheme
        if (!(scheme == SCHEME_KSR || scheme == SCHEME_HTTPS)) {
            return null
        }
        val matcher = PROJECT_PATTERN.matcher(uri.path)
        return if (matcher.matches() && matcher.group(3) != null) {
            matcher.group(3)
        } else null
    }

    /**
     * check the project save query from a uri. e.g.: A uri like `ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee?save=true`
     * returns true or false
     */
    fun hasSaveQueryFromUri(uri: Uri?): Boolean {
        if (uri == null) {
            return false
        }
        val scheme = uri.scheme
        if (!(scheme == SCHEME_KSR || scheme == SCHEME_HTTPS)) {
            return false
        }
        val matcher = PROJECT_PATTERN.matcher(uri.path)
        val query = PROJECT_SAVE_QUERY_PATTERN.matcher(uri.query()).matches()
        return matcher.matches() && matcher.group(3) != null && query
    }
}
