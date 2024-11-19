package com.kickstarter.ui.adapters

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.models.Category
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.fragments.DiscoveryFragment
import io.reactivex.Observable

class DiscoveryPagerAdapter(
    fragmentManager: FragmentManager,
    private val fragments: MutableList<DiscoveryFragment>,
    private val pageTitles: List<String>,
    private val delegate: Delegate
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    interface Delegate {
        fun discoveryPagerAdapterSetPrimaryPage(adapter: DiscoveryPagerAdapter, position: Int)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        delegate.discoveryPagerAdapterSetPrimaryPage(this, position)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as DiscoveryFragment
        fragments[position] = fragment
        return fragment
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return DiscoveryParams.Sort.defaultSorts.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pageTitles[position]
    }

    /**
     * Passes along root categories to its fragment position to help fetch appropriate projects.
     */
    fun takeCategoriesForPosition(categories: List<Category>, position: Int) {
        Observable.fromIterable(fragments)
            .filter(DiscoveryFragment::isInstantiated)
            .filter(DiscoveryFragment::isAttached)
            .filter { frag: DiscoveryFragment ->
                val fragmentPosition = frag.arguments?.getInt(ArgumentsKey.DISCOVERY_SORT_POSITION)
                fragmentPosition == position
            }
            .map { frag: DiscoveryFragment -> frag.takeCategories(categories) }
            .subscribe()
    }

    /**
     * Take current params from activity and pass to the appropriate fragment.
     */
    fun takeParams(params: DiscoveryParams) {
        Observable.fromIterable(fragments)
            .filter(DiscoveryFragment::isInstantiated)
            .filter(DiscoveryFragment::isAttached)
            .filter { frag: DiscoveryFragment ->
                val fragmentPosition = frag.arguments?.getInt(ArgumentsKey.DISCOVERY_SORT_POSITION)
                params.sort().positionFromSort() == fragmentPosition
            }
            .map { frag: DiscoveryFragment -> frag.updateParams(params) }
            .subscribe()
    }

    /**
     * Call when the view model tells us to clear specific pages.
     */
    fun clearPages(pages: List<Int?>) {
        Observable.fromIterable(fragments)
            .filter(DiscoveryFragment::isInstantiated)
            .filter(DiscoveryFragment::isAttached)
            .filter { frag: DiscoveryFragment ->
                val fragmentPosition = frag.arguments?.getInt(ArgumentsKey.DISCOVERY_SORT_POSITION)
                pages.contains(fragmentPosition)
            }
            .map { obj: DiscoveryFragment -> obj.clearPage() }
            .subscribe()
    }

    fun scrollToTop(position: Int) {
        Observable.fromIterable(fragments)
            .filter(DiscoveryFragment::isInstantiated)
            .filter(DiscoveryFragment::isAttached)
            .filter { frag: DiscoveryFragment ->
                val fragmentPosition = frag.arguments?.getInt(ArgumentsKey.DISCOVERY_SORT_POSITION)
                position == fragmentPosition
            }
            .map { obj: DiscoveryFragment -> obj.scrollToTop() }
            .subscribe()
    }
}
