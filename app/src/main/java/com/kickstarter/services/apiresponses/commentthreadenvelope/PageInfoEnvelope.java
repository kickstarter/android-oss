package com.kickstarter.services.apiresponses.commentthreadenvelope;

import android.os.Parcelable;
import com.kickstarter.libs.qualifiers.AutoGson;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class PageInfoEnvelope implements Parcelable {
    public abstract Boolean hasPreviousPage();
    public abstract Boolean hasNextPage();
    public abstract String startCursor();
    public abstract String endCursor();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract  Builder hasPreviousPage(Boolean __);
        public abstract  Builder hasNextPage(Boolean __);
        public abstract  Builder startCursor(String __);
        public abstract  Builder endCursor(String __);
        public abstract PageInfoEnvelope build();
    }

    public static Builder builder() {
            return new AutoParcel_PageInfoEnvelope.Builder();
    }

    public abstract Builder toBuilder();
}
