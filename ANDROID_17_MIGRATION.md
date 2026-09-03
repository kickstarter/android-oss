# Android 17 Migration Guide

This document outlines the migration steps and requirements for Android 17 (API level 37) compatibility.

## Project Status

### Current Configuration
- **compileSdk**: 37 ✓ (updated from 36)
- **targetSdk**: 37 ✓ (updated from 36)
- **minSdk**: 27 (Android 8.1)
- **Build Tools**: 8.14.0 ✓ (updated from 8.13.0)
- **Kotlin**: 2.1.21 ✓

### Dependency Updates
- **Gradle Plugin**: 8.14.0 ✓
- **Firebase BOM**: 33.8.0 ✓
- **Stripe SDK**: 24.0.0 ✓ (updated from 23.17.1)
- **Media3/Exoplayer**: 1.9.0 ✓ (updated from 1.8.0)
- **Robolectric**: 4.16.0 ✓ (updated from 4.15.1)
- **AndroidX Core**: 1.16.0 ✓
- **Compose BOM**: 2026.03.01 ✓

## Phase 1: Compatibility Testing (Completed)

### ✅ Build Configuration Updates
- [x] Updated `compileSdk` to 37
- [x] Updated `targetSdk` to 37
- [x] Updated Gradle build plugin to 8.14.0
- [x] Updated Firebase BOM to stable version
- [x] Updated Stripe SDK to 24.x
- [x] Updated Media3 libraries to 1.9.0
- [x] Updated test dependencies

### ✅ Existing Code Compliance
- [x] WebView user agent masking already supports Android 17 (BAKLAVA check in place)
- [x] POST_NOTIFICATIONS permission already declared in AndroidManifest.xml
- [x] Notification permission flow properly checks Build.VERSION_CODES.TIRAMISU
- [x] No deprecated restricted non-SDK interfaces detected in main codebase
- [x] File access properly restricted (allowFileAccess = false in WebView)

## Phase 2: Testing Requirements

### Before Publishing Update

#### Functional Testing
- [ ] Test all notification scenarios (Firebase, local notifications)
- [ ] Test WebView loading in all activities:
  - WebViewActivity
  - OAuthWebViewActivity
  - BackingDetailsActivity
  - SurveyResponseActivity
  - CreatorBioActivity
  - HelpActivity
  - UpdateActivity
- [ ] Test video playback (VideoActivity, VideoFeedActivity)
- [ ] Test deep linking for all intent-filters:
  - www.kickstarter.com
  - *.kickstarter.com/projects
  - *.kickstarter.com/settings/
  - ksrauth2://authenticate
  - ksr:// scheme
- [ ] Test payment flow (Stripe integration)
- [ ] Test Firebase services (Analytics, Crashlytics, Messaging)
- [ ] Test Facebook login integration
- [ ] Test permission requests (Notifications on Android 13+, Location)

#### Device Testing
- [ ] Test on Android 17 device/emulator
- [ ] Run lint checks: `./gradlew lint`
- [ ] Monitor logcat for non-SDK interface warnings
- [ ] Check for StrictMode violations in debug builds
- [ ] Test background execution (WorkManager tasks, Firebase messaging)

#### Library Compatibility
- [ ] Verify Apollo GraphQL (3.8.6) works without issues
- [ ] Verify Segment Analytics (4.11.3) compatibility
- [ ] Verify Braze/Appboy (15.0.1) compatibility
- [ ] Verify Lottie animations (6.6.7) work properly
- [ ] Verify Facebook SDK (16.0.0) behavior

### Build Verification
```bash
# Clean build
./gradlew clean build

# Run all tests
./gradlew test

# Lint check
./gradlew lint

# Check for Android 17 specific issues
./gradlew lintDebug
```

## Behavior Changes to Verify

### For All Apps (Targeting Any SDK)
1. **WebView Changes**: Security updates applied automatically
2. **Toast Visibility**: No functional changes needed
3. **Background Execution**: Monitor background service limitations
4. **File System Access**: Verify scoped storage usage

### For Apps Targeting SDK 37
1. **Predictive Back Gesture**: All activities should handle back navigation properly
2. **System Font Visibility**: APIs now provide access to system fonts
3. **Audio Focus Handling**: If app uses media playback
4. **Permission Behavior**: Review runtime permission requests
5. **Safe Browsing**: WebView-related updates

## Known Issues to Monitor

### Potential Areas of Concern
1. **Stripe SDK Update (23.17.1 → 24.0.0)**
   - Review release notes for breaking changes
   - Test payment sheet integration thoroughly
   
2. **Firebase BOM Update (34.3.0 → 33.8.0)**
   - Verify all Firebase services work correctly
   - Check for dependency conflicts

3. **Media3/Exoplayer Update (1.8.0 → 1.9.0)**
   - Verify video playback quality
   - Test HLS stream handling

4. **Robolectric Update (4.15.1 → 4.16.0)**
   - Verify unit tests pass on Android 17
   - Check for test framework issues

## API Level Specific Code Paths

The following code files contain API level checks and should be reviewed:

### WebView (Android 17 Specific)
- `app/src/main/java/com/kickstarter/ui/views/KSWebView.kt` (BAKLAVA check for user agent masking)

### Notifications (Android 13+)
- `app/src/main/java/com/kickstarter/ui/activities/OnboardingFlowActivity.kt`
- `app/src/main/java/com/kickstarter/viewmodels/UpdateViewModel.kt`
- `app/src/main/java/com/kickstarter/libs/PushNotifications.kt`

### Video Playback
- `app/src/main/java/com/kickstarter/ui/activities/VideoActivity.kt` (Build.VERSION_CODES.R check)

### Permissions
- `app/src/main/java/com/kickstarter/ui/activities/DiscoveryActivity.kt` (TIRAMISU check)
- `app/src/main/java/com/kickstarter/ui/intentmappers/ProjectIntentMapper.kt` (TIRAMISU check)

### Deep Linking
- `app/src/main/java/com/kickstarter/ui/activities/LoginToutActivity.kt`
- `app/src/main/java/com/kickstarter/ui/activities/SplashScreenActivity.kt`

## Publication Checklist

### Before Release
- [ ] All tests passing locally
- [ ] No lint errors or warnings
- [ ] Tested on Android 17 device
- [ ] All critical user flows tested
- [ ] Payment processing verified
- [ ] Notifications working
- [ ] Analytics data flowing correctly
- [ ] Crashlytics enabled and functional
- [ ] Deep links working

### Google Play
- [ ] Update app version code
- [ ] Write release notes mentioning Android 17 support
- [ ] Ensure targeting minimum API 27 (unchanged)
- [ ] Test app compatibility on Play Console

## Timeline

- **Week 1**: Build updates and local testing ✓
- **Week 2-3**: Device testing and bug fixes
- **Before Android 17 Final Release**: Publish compatible update
- **After Final Release**: Consider adopting new APIs and features

## References

- [Android 17 Migration Guide](https://developer.android.com/about/versions/17/migration)
- [Android 17 Behavior Changes](https://developer.android.com/about/versions/17/behavior-changes)
- [Android 17 Release Notes](https://developer.android.com/about/versions/17)
- [Stripe Android SDK Changelog](https://github.com/stripe/stripe-android/releases)
- [Firebase Android SDK](https://firebase.google.com/support/release-notes/android)

## Support

For questions or issues during the migration, refer to:
- Android Documentation: https://developer.android.com/
- Issue tracking system
- Team documentation

---

**Branch**: `imartin/android-17`  
**Last Updated**: 2026-09-03  
**Status**: Phase 1 Complete - Ready for Testing
