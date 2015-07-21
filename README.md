# Getting Started

1. Clone this repository.
2. [Install Android Studio](https://developer.android.com/sdk/index.html)
3. Add the Android tools to your `PATH` for convenience when working in the
   shell, e.g. in your `.bashrc`:

   ```bash
   export PATH=${HOME}/Library/Android/sdk/platform-tools:${HOME}/Library/Android/sdk/tools:$PATH
   ```

4. Bootstrap your Android development environment by running `script/bootstrap`.
5. Import the project. Open Android Studio, then click `File > Import Project` and
   select `build.gradle` in the root of the repository.
6. Download the [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Currently using 8.
6. Start up the app. Click `Run > Run 'app'`. After the project builds you'll be
   prompted to build or launch an emulator - use `Nexus 5 API 22 x86` for the
   device.

# Next Steps

Check the [wiki](http://kickstarter.wiki/pages/native.html#android-guides) for further
reading.
