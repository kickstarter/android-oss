package com.kickstarter.services.apiresponses.commentthreadenvelope;
import com.kickstarter.libs.qualifiers.AutoGson;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ProjectCommentsEnvelope {
    public abstract CommentsWrapper comments();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Builder comments(CommentsWrapper __);
        public abstract ProjectCommentsEnvelope build();
    }

    public static Builder builder() {
        return new AutoParcel_ProjectCommentsEnvelope.Builder();
    }

    public abstract Builder toBuilder();
}
