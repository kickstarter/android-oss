package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.activities.HelpActivity;
import com.kickstarter.ui.views.IconTextView;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ActivityFeedToolbar extends KSToolbar {
  @Bind(R.id.more_button) IconTextView moreButton;
  @BindString(R.string.___Not_implemented_yet) String notImplementedYetString;

  public ActivityFeedToolbar(@NonNull final Context context) {
    super(context);
  }

  public ActivityFeedToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public ActivityFeedToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @OnClick(R.id.more_button)
  protected void moreButtonClick() {
    final BaseActivity activity = (BaseActivity) getContext();
    final PopupMenu popup = new PopupMenu(activity, moreButton);
    popup.getMenuInflater().inflate(R.menu.activity_feed_menu, popup.getMenu());
    popup.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {
        case R.id.find_friends:
          // TODO
          ViewUtils.showToast(activity, notImplementedYetString);
          break;
        case R.id.help:
          final Intent helpIntent = new Intent(activity, HelpActivity.class);
          helpIntent.putExtra(activity.getString(R.string.intent_help_type), HelpActivity.HELP_TYPE_GENERAL);
          activity.startActivity(helpIntent);
          break;
      }

      return true;
    });

    popup.show();
  }
}
