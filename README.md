# Getting Started

1. Clone this repository
2. [Install Android Studio](https://developer.android.com/sdk/index.html)
3. Add the Android tools to your `PATH` for convenience when working in the
   shell, e.g. in your `.bashrc`:

   ```bash
   export PATH=${HOME}/Library/Android/sdk/platform-tools:${HOME}/Library/Android/sdk/tools:$PATH
   ```

4. Apply the Kickstarter styles by running `script/bootstrap`
5. Import the project. Open Android Studio, then click `File > Import Project` and
   select `build.gradle` in the root of the repository
6. Start up the app. Click `Run > Run 'app'`. After the project builds you'll be
   prompted to build or launch an emulator - use `Nexus 5 API 22 x86` for the
   device.

# Logging

We use Timber for logging. In production it's smart enough to no-op. You can
view log output in Android Studio's logcat window, but it's kinda janky. For
a better way to view logs, the bootstrap script installs `pidcat`.

You can get the log firehose by running:

```
pidcat com.kickstarter.kickstarter.internal.debug
```

Or filter using tags (regexps allowed):

```
pidcat -t "\w*Activity" com.kickstarter.dev
```

If you encounter an error when starting pidcat, make sure the Android platform tools
are in your `PATH`.

# Access local Kickstarter via an emulator

We can make this easier, but for now:

1. In the Kickstarter app, edit `config/config.yml` so that the API url matches
   the endpoint your emulator will use, e.g.: `api_url: 'api.ksr.10.0.3.2.xip.io'`
2. Ensure `config/initializers/canonical_host_middleware.rb` is ignoring the
   endpoints the emulator will use to hit the site and API, e.g. `ksr.10.0.3.2.xip.io`
   and `api.ksr.10.0.3.2.xip.io`.
3. Edit the endpoints in the Android app's `KickstarterClient` and `ApiClient`
   classes so they point to the correct location.

If it's not working properly, you can use your emulator's browser to hit
endpoints and debug.
