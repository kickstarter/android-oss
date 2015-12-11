package com.kickstarter.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kickstarter.R;
import com.kickstarter.ui.viewholders.LoggedInMenuProfileViewHolder;
import com.kickstarter.ui.viewholders.LoggedInMenuViewHolder;

import java.util.ArrayList;
import java.util.List;

public final class LoggedInMenuAdapter extends BaseAdapter {
  public static final int TYPE_PROFILE = 0;
  public static final int TYPE_FIND_FRIENDS = 1;
  public static final int TYPE_SETTINGS = 2;
  public static final int TYPE_HELP = 3;
  public static final int TYPE_LOGOUT = 4; // TODO: remove after merge Settings

  private final Context context;

  private List<String> titles = new ArrayList<>();

  public LoggedInMenuAdapter(final @NonNull Context context) {
    this.context = context;
  }

  public void takeTitle(final @NonNull String title) {
    titles.add(title);
    notifyDataSetChanged();
  }

  @Override
  public boolean areAllItemsEnabled() {
    return true;
  }

  @Override
  public boolean isEnabled(final int position) {
    return true;
  }

  @Override
  public int getCount() {
    return titles.size();
  }

  @Override
  public String getItem(final int position) {
    return titles.get(position);
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
        case TYPE_PROFILE:
          convertView = LayoutInflater.from(context).inflate(R.layout.logged_in_menu_avatar_item, null);
          final LoggedInMenuProfileViewHolder profileViewHolder = new LoggedInMenuProfileViewHolder(convertView);
          profileViewHolder.setTitle(getItem(position));
          convertView.setTag(profileViewHolder);
          break;
        case TYPE_FIND_FRIENDS:
        case TYPE_SETTINGS:
        case TYPE_HELP:
        case TYPE_LOGOUT:
          convertView = LayoutInflater.from(context).inflate(R.layout.logged_in_menu_item, null);
          final LoggedInMenuViewHolder defaultViewHolder = new LoggedInMenuViewHolder(convertView);
          defaultViewHolder.setTitle(getItem(position));
          convertView.setTag(defaultViewHolder);
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
  public boolean isEmpty() {
    return false;
  }
}
