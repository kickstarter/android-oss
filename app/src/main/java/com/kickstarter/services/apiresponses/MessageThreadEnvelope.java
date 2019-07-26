package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.User;

import java.util.List;

import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class MessageThreadEnvelope implements Parcelable {
  public abstract @Nullable List<Message> messages();
  public abstract @Nullable MessageThread messageThread();
  public abstract List<User> participants();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder messages(List<Message> __);
    public abstract Builder messageThread(MessageThread __);
    public abstract Builder participants(List<User> __);
    public abstract MessageThreadEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_MessageThreadEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
