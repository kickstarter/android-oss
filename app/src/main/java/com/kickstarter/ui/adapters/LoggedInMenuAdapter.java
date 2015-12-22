package com.kickstarter.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kickstarter.R;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.LoggedInMenuProfileViewHolder;
import com.kickstarter.ui.viewholders.LoggedInMenuViewHolder;

import java.util.ArrayList;
import java.util.List;

public final class LoggedInMenuAdapter extends BaseAdapter {
  public static final int ROW_PROFILE = 0;
  public static final int ROW_SETTINGS = 1;
  public static final int ROW_HELP = 2;

  private final Context context;
  private final User user;

  private List<String> rows = new ArrayList<>();

  public LoggedInMenuAdapter(final @NonNull Context context, final @NonNull User user) {
    this.context = context;
    this.user = user;

    rows.add(ROW_PROFILE, user.name());
    rows.add(ROW_SETTINGS, context.getResources().getString(R.string.___Settings));
    rows.add(ROW_HELP, context.getResources().getString(R.string.___Help));
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return rows.size();
  }

  @Override
  public String getItem(final int position) {
    return rows.get(position);
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  @Override
  public View getView(final int position, @Nullable View convertView, final @NonNull ViewGroup parent) {
    int type = getItemViewType(position);
    if (convertView == null) {
      switch (type) {
        case ROW_PROFILE:
          convertView = LayoutInflater.from(context).inflate(R.layout.logged_in_menu_avatar_item, parent, false);
          convertView.setTag(new LoggedInMenuProfileViewHolder(convertView, getItem(position), user.avatar()));
          break;
        case ROW_SETTINGS:
        case ROW_HELP:
          convertView = LayoutInflater.from(context).inflate(R.layout.logged_in_menu_item, parent, false);
          convertView.setTag(new LoggedInMenuViewHolder(convertView, getItem(position)));
          break;
      }
    }
    return convertView;
  }

  @Override
  public int getItemViewType(final int position) {
    return position;
  }

  @Override
  public int getViewTypeCount() {
    return rows.size();
  }
}
