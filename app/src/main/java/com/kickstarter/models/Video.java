package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class Video implements Parcelable {
    String high = null;
    String base = null;
    String webm = null;

    public String frame() {
        return frame;
    }

    public String high() {
        return high;
    }

    public String base() {
        return base;
    }

    public String webm() {
        return webm;
    }

    String frame = null;    // image layover

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        VideoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        public Video createFromParcel(Parcel source) {
            Video target = new Video();
            VideoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}
