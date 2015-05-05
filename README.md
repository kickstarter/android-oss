# Getting Started

1. Clone this repository
2. [Install Android Studio](https://developer.android.com/sdk/index.html)
3. Apply the Kickstarter styles by running `script/bootstrap`
4. Import the project. Open Android Studio, then click `File > Import Project` and
   select `build.gradle` in the root of the repository
5. Start up the app. Click `Run > Run 'app'`. After the project builds you'll be
   prompted to build or launch an emulator - use `Nexus 5 API 21 x86` for the
   device.

# Logging

We use Timber for logging. In production it's smart enough to no-op. You can
view log output in Android Studio's logcat window, but it's kinda janky. For
a better way to view logs, the bootstrap script installs `pidcat`.

You can get the log firehose by running:

```
pidcat com.kickstarter.dev
```

Or filter using tags (regexps allowed):

```
pidcat -t "\w*Activity" com.kickstarter.dev
```
