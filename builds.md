# Builds

We have many build variants, the variant depends on the build type and product
flavor.

## Build types

There are two build types: `debug` and `release`. In local development we'll
typically be generating debug builds. Release builds are for when we want to
distribute the application outside the development team. The [Android Tools
site](http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Build-Types)
has more on the difference between debug and release builds. Debug builds will
have features that assist with debugging and testing, like the ability to change
endpoints.

## Product flavors

There are two product flavors: `internal` and `external`. The internal product
flavor is for distribution to internal or trusted users. In iOS, we use beta
builds for this purpose.

## Build variants

* `internalDebug`: Mobile product team
* `internalRelease`: Kickstarter staff
* `externalDebug`: Likely won't be used much, could be useful if we establish
  a formal QA process as a check before public release
* `externalRelease`: Public users, distributed via Play store

## Signing packages

Debug builds are signed using `app/debug.keystore`. Creating debug builds
requires no configuration; they're signed with the default Android username and
password.

Release builds are signed using `app/kickstarter.keystore`. The credentials
should be stored in `app/signing.gradle`. We don't want to commit the keystore
or credentials into source control, so a manual process is required to set them
up.

The keystore is backed up in S3:
`s3://android-ksr-keystores/kickstarter.keystore`. The credentials are stored
in passpack under `Android - Keystore`. The keystore can be copied from
S3 into `app/kickstarter.keystore`, and the credentials saved into `app/signing.gradle`.
The keystore and credentials are in `.gitignore` so local changes won't be committed.

# Releasing

There's a command-line tool in this repository named Milkrun. Run `bin/milkrun
--help` to get started, more documentation on this to come.
