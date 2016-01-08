# Getting Started

1. Clone this repository.
2. Download the [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Currently using 8.
3. [Install Android Studio](https://developer.android.com/sdk/index.html)
4. Add the Android tools to your `PATH` for convenience when working in the
   shell, e.g. in your `.bash_profile`:

   ```bash
   export PATH=${HOME}/Library/Android/sdk/platform-tools:${HOME}/Library/Android/sdk/tools:$PATH
   ```

5. Bootstrap your Android development environment by running `script/bootstrap`.
   Keep an eye on the output to see if any manual steps are required.
6. Import the project. Open Android Studio, then click `File > Import Project` and
   select `build.gradle` in the root of the repository.
7. Switch the Android build variant to `internalMin21Debug` using `View > Tool Windows > Build Variants`.
8. Start up the app. Click `Run > Run 'app'`. After the project builds you'll be
   prompted to build or launch an emulator - use `Nexus 5 API 23 x86` for the
   device.

# Next Steps

Check the [wiki](https://kickstarter.wiki/pages/native.html#android) for further
reading.
