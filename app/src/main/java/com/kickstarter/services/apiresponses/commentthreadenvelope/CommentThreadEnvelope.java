package com.kickstarter.services.apiresponses.commentthreadenvelope;

import com.kickstarter.libs.qualifiers.AutoGson;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class CommentThreadEnvelope {

    public abstract ProjectDataEnvelope data();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Builder data(ProjectDataEnvelope __);
        public abstract CommentThreadEnvelope build();
    }

    public static Builder builder() {
        return new AutoParcel_CommentThreadEnvelope.Builder();
    }

    public abstract Builder toBuilder();

    @AutoGson
    @AutoParcel
    public abstract static class ProjectDataEnvelope {

        public abstract ProjectCommentsEnvelope project();

        @AutoParcel.Builder
        public abstract static class Builder {
            public abstract Builder project(ProjectCommentsEnvelope __);
            public abstract ProjectDataEnvelope build();
        }

        public static Builder builder() {
            return new AutoParcel_CommentThreadEnvelope_ProjectDataEnvelope.Builder();
        }

        public abstract Builder toBuilder();
   }
}


