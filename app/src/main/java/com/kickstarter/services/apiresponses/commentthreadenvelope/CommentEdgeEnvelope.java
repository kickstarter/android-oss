package com.kickstarter.services.apiresponses.commentthreadenvelope;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.CommentThread;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class CommentEdgeEnvelope implements Parcelable {
    public abstract CommentThread node();
    public abstract String cursor();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Builder node(CommentThread __);
        public abstract Builder cursor(String __);
        public abstract CommentEdgeEnvelope build();
    }

    public static Builder builder() {
        return new AutoParcel_CommentEdgeEnvelope.Builder();
    }

    public abstract Builder toBuilder();
}
