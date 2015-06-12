# Builds

We have many kinds of packages, the package depends on the build type and product
flavor.

## Build types

There are two build types: `debug` and `release`. We'll typically be generating
debug builds in local development. Release builds are for when we want to
distribute the application to other users. The [Android Tools
site](http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Build-Types)
has more on the difference between debug and release builds.

## Product flavors

There are two product flavors: `internal` and `external`. The internal product
flavor is for distribution to internal or trusted users. In iOS, we use beta
builds for this purpose. The internal build will have features that assist with
debugging and testing, like the ability to change endpoints.

External builds are for distribution on the Google Play store.

## Signing packages

Debug builds are signed using `app/debug.keystore`. Creating debug builds
requires no configuration; they're signed with the default Android username and
password.

Release builds are signed using `app/kickstarter.keystore`. The credentials
should be stored in `app/signing.gradle`. We don't want to commit the keystore
into source control, because if somebody obtained it they could potentially
bruteforce the password. We also don't want to commit the credentials - if our
keystore was exposed, it would make accessing it much easier.

The keystore is backed up in S3:
`s3://android-ksr-keystores/kickstarter.keystore`. The credentials are stored
in passpack under `Android - Keystore`. The keystore can be copied from
S3 into `app/kickstarter.keystore`, and the credentials saved into `app/signing.gradle`.
The keystore and credentials are in `.gitignore` so local changes won't be committed.
