#!/bin/bash

# Install the required tools and the emulator itself
sdkmanager "platform-tools" "platforms;android-30" "build-tools;30.0.3" "emulator"
# Install system images for the emulator
sdkmanager "system-images;android-30;google_apis;x86"
# Create an emulator with the installed system images
echo no | avdmanager create avd -n test-emulator -k "system-images;android-30;google_apis;x86"
# Start the emulator with no audio, boot animation, window, and with GPU acceleration off
emulator -avd test-emulator -noaudio -no-boot-anim -gpu off -no-window &
# Wait for the emulator to boot completely
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;'
# Dismiss the emulator lock screen and wait 1 second for it to settle
adb shell wm dismiss-keyguard
sleep 1
# Disable window and transition animations. This is required to run UI tests correctly
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
# Configuration to be able to do Screenshot Test on API higher that 27
adb shell settings put global hidden_api_policy_p_apps 1
adb shell settings put global hidden_api_policy_pre_p_apps 1
adb shell settings put global hidden_api_policy  1
