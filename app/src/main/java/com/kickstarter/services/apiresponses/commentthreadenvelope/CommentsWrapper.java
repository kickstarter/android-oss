package com.kickstarter.services.apiresponses.commentthreadenvelope;

import com.kickstarter.libs.qualifiers.AutoGson;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class CommentsWrapper {

    public abstract Integer totalCount();
    public abstract PageInfoEnvelope pageInfo();
    public abstract List<CommentEdgeEnvelope> edges();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Builder totalCount(Integer __);
        public abstract Builder pageInfo(PageInfoEnvelope __);
        public abstract Builder edges(List<CommentEdgeEnvelope> __);
        public abstract CommentsWrapper build();
    }

    public static Builder builder() {
        return new AutoParcel_CommentsWrapper.Builder();
    }

    public abstract Builder toBuilder();
}
