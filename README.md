# Integration with Waldo

This repo is a fork of the excellent [Kickstarter app](https://github.com/kickstarter/android-oss).

We stripped their README in favor of this integration guide. This demonstrates how you can very easily integrate with [Waldo](https://www.waldo.io/) in just a few steps.


## Main concept

Waldo automates the run of realistic scenarios in different versions of your app.
More specifically, at any point you can upload a version of your app and get it compared with the previous versions to spot any regression.

This typically works in 2 stages:

- **beta** just before, or along with sharing an internal beta with your team, you probably want to see how the app behaves compared to the previous release.

- **merge** when the app gets merged into the main branch (meaning it's an incremental addition to what will ultimately constitute the next release) it should be uploaded as the basis for any new test.

You can easily automate these 2 stages in your Continuous Integration cycle.


## Configure your repo with Waldo

Before anything, you will need to configure your repo to properly upload the APKs to Waldo so we can automate your testing.

These steps have to be done only once for your repo, and only takes 2 minutes.
They are based off of our [Gradle plugin](https://github.com/waldoapp/gradle-plugin).
Ultimately, the steps will only be as big as [this commit](https://github.com/waldoapp/android-oss/commit/ae0e8c1bb03ffc83167547cc12f4de825aa93f97)


### Register our plugin in your app

1. Open your project in Android Studio. On the left sidebar, open the Project View, expand the `Gradle Scripts` section, and click on `build.gradle (Module: app)`.

<img width="369" alt="open_build_gradle" src="https://user-images.githubusercontent.com/10992081/35189064-fb196388-fe3a-11e7-8a26-fa19e238e03a.png">
<img width="369" alt="open_build_gradle_boxed" src="https://user-images.githubusercontent.com/10992081/35189065-fb316028-fe3a-11e7-9a84-a3dfda66f3be.png">

2. In the `buildScript` section, add the gradle repository and our plugin ID. To see the latest version, refer to our [Gradle Page](https://plugins.gradle.org/plugin/io.waldo.tools).

<img width="758" alt="register_our_plugin" src="https://user-images.githubusercontent.com/10992081/35189087-5b8ca270-fe3b-11e7-9336-863914800847.png">
<img width="758" alt="register_our_plugin_boxed" src="https://user-images.githubusercontent.com/10992081/35189088-5ba53880-fe3b-11e7-9427-45b678e392c5.png">

3. Apply our plugin to your build, and make sure you do so **after** the plugin `com.android.application` is declared


<img width="766" alt="apply_our_plugin" src="https://user-images.githubusercontent.com/10992081/35189097-96d1faf6-fe3b-11e7-8c5f-d629ad58befd.png">
<img width="766" alt="apply_our_plugin_boxed" src="https://user-images.githubusercontent.com/10992081/35189098-96e980c2-fe3b-11e7-9b42-6f88eae44737.png">

### Register your Waldo credentials

Now at the bottom of the `build.gradle`, enter your credentials. You need to define
```
waldo {
    apiKey: string
    applicationId: string
}
```
<img width="759" alt="add_credentials" src="https://user-images.githubusercontent.com/10992081/35189107-dd3fc220-fe3b-11e7-9e1e-acdf877b6576.png">

It is a good practice to keep these credentials out of the build.gradle, by declaring them in a `grade.properties` file for instance. This is what we have done in the example above.

### (optional) Create a Build configuration in your project

Now you have declared our plugin in your `build.gradle`.
Specifically the plugin creates one task per Android flavor that you have declared. For instance, if you have buildType = `release` and `debug`, you have now 2 more tasks available, `uploadDebugToWaldo` and `uploadReleaseToWaldo`.

To create an alias for one of these tasks, click on `Edit configurations` in the top sidebar.

<img width="1350" alt="screen shot 2018-01-20 at 11 51 32 pm" src="https://user-images.githubusercontent.com/10992081/35189223-fe17c34c-fe3d-11e7-9b65-448900521d07.png">

Then click on `+` and select `Gradle`.

<img width="323" alt="screen shot 2018-01-20 at 11 52 01 pm" src="https://user-images.githubusercontent.com/10992081/35189224-fe2fdfb8-fe3d-11e7-833a-6e5b424f98b3.png">

You can name the configuration `Upload to Waldo` for instance. As you can see, several tasks are available.

<img width="1062" alt="screen shot 2018-01-20 at 11 52 42 pm" src="https://user-images.githubusercontent.com/10992081/35189225-fe48ba1a-fe3d-11e7-8bce-88a6668da71f.png">

Select the one you prefer, click `Apply` and `OK`.

Now, whenever you have a new version of the app that you want compared with the previous release, you can click `Run` for this configuration!


## Continuous Integration

If you have a Continuous integration that supports gradle, you can hook any step to upload to Waldo for automatic testing. For instance:

```
./gradlew uploadDebugToWaldo
```
