<a href="https://www.kickstarter.com"><img src=".github/ksr-wordmark.svg" width="100%" alt="Kickstarter for Android™"/>

[![CircleCI](https://circleci.com/gh/kickstarter/android-oss.svg?style=svg)](https://circleci.com/gh/kickstarter/android-oss)

Welcome to Kickstarter's open source Android app! Come on in, take your shoes
off, stay a while—_explore_ how Kickstarter's native squad has built and
continues to build the app, _discover_ our implementation of [RxJava](https://github.com/ReactiveX/RxJava) in logic-
filled [view models](https://github.com/kickstarter/android-oss/tree/master/app/src/main/java/com/kickstarter/viewmodels),
and maybe even _create_ an issue or two.

We've also open sourced our iOS app, written in Swift:
[check it out here](https://github.com/kickstarter/ios-oss). Read more about our journey to open source [here](https://kickstarter.engineering/open-sourcing-our-android-and-ios-apps-6891be909fcd#.o1fe86s6w).

## Getting Started

_Follow these instructions to build and run the project with mock data.._

1. Clone this repository.
2. Download the appropriate [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
for your system. We are currently on JDK 8.
3. [Install Android Studio](https://developer.android.com/sdk/index.html).
4. `cd` into the project repo and run `make bootstrap` to bootstrap your Android
   development environment. Keep an eye on the output to see if any manual steps
   are required.
5. Import the project. Open Android Studio, click `Open an existing Android
   Studio project` and select the project. Gradle will build the project.
6. Run the app. Click `Run > Run 'app'`. After the project builds you'll be
   prompted to build or launch an emulator.
   
## Documentation

While we're at it, why not share our docs? Check out the
[native docs](https://github.com/kickstarter/native-docs) we have written so far
for more documentation.

## Contributing

We intend for this project to be an educational resource: we are excited to
share our wins, mistakes, and methodology of Android development as we work
in the open. Our primary focus is to continue improving the app for our users in
line with our roadmap.

The best way to submit feedback and report bugs is to open a Github issue.
Please be sure to include your operating system, device, version number, and
steps to reproduce reported bugs. Keep in mind that all participants will be
expected to follow our code of conduct.

## Code of Conduct

We aim to share our knowledge and findings as we work daily to improve our
product, for our community, in a safe and open space. We work as we live, as
kind and considerate human beings who learn and grow from giving and receiving
positive, constructive feedback. We reserve the right to delete or ban any
behavior violating this base foundation of respect.

## Find this interesting?

We do too, and we’re [hiring](https://www.kickstarter.com/jobs?ref=gh_android_oss)!

## License

```
Copyright 2019 Kickstarter, PBC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
