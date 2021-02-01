package com.kickstarter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.EmptyViewBinding;
import com.kickstarter.databinding.ProjectCardViewBinding;
import com.kickstarter.databinding.ThanksCategoryViewBinding;
import com.kickstarter.databinding.ThanksShareViewBinding;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.data.ThanksData;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder;
import com.kickstarter.ui.viewholders.ThanksShareViewHolder;

import java.util.Collections;

public final class ThanksAdapter extends KSAdapter {
  private static final int SECTION_SHARE_VIEW = 0;
  private static final int SECTION_RECOMMENDED_PROJECTS_VIEW = 1;
  private static final int SECTION_CATEGORY_VIEW = 2;

  public final Delegate delegate;

  public interface Delegate extends ProjectCardViewHolder.Delegate, ThanksCategoryViewHolder.Delegate {}

  public ThanksAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
    insertSection(SECTION_SHARE_VIEW, Collections.emptyList());
    insertSection(SECTION_RECOMMENDED_PROJECTS_VIEW, Collections.emptyList());
    insertSection(SECTION_CATEGORY_VIEW, Collections.emptyList());
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == SECTION_SHARE_VIEW) {
      return R.layout.thanks_share_view;
    } else if (sectionRow.section() == SECTION_RECOMMENDED_PROJECTS_VIEW) {
      return R.layout.project_card_view;
    } else {
      return R.layout.thanks_category_view;
    }
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    switch(layout) {
      case R.layout.thanks_share_view:
        return new ThanksShareViewHolder(ThanksShareViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
      case R.layout.project_card_view:
        return new ProjectCardViewHolder(ProjectCardViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false), this.delegate);
      case R.layout.thanks_category_view:
        return new ThanksCategoryViewHolder(ThanksCategoryViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false), this.delegate);
      default:
        return new EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }
  }

  public void takeData(final @NonNull ThanksData data) {
    setSection(SECTION_SHARE_VIEW, Collections.singletonList(data.getBackedProject()));
    setSection(SECTION_RECOMMENDED_PROJECTS_VIEW, ProjectUtils.combineProjectsAndParams(data.getRecommendedProjects(), DiscoveryParams.builder().build()));
    setSection(SECTION_CATEGORY_VIEW, Collections.singletonList(data.getCategory()));
    notifyDataSetChanged();
  }
}
