#!/bin/bash

if $ANDROID_HOME/tools/android list avd | grep -q Pixel_3a_API_30; then
    echo "There is an existing an emulator to run screenshot tests"
    exit 0;
fi

echo "Creating a brand new SDCard..."
rm -rf sdcard.img
$ANDROID_HOME/emulator/mksdcard -l e 1G sdcard.img
echo "SDCard created!"

echo "Downloading the image to create the emulator..."
echo no | $ANDROID_HOME/tools/bin/sdkmanager "system-images;android-30;google_apis;x86"
echo "Image downloaded!"

echo "Creating the emulator to run screenshot tests..."
echo no | $ANDROID_HOME/tools/bin/avdmanager create avd -n Pixel_3a_API_30 -k "system-images;android-30;google_apis;x86" --force --sdcard sdcard.img
echo "Emulator created!"


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cp $DIR/config.ini ~/.android/avd/Pixel_3a_API_30.avd/config.ini
cp sdcard.img ~/.android/avd/Pixel_3a_API_30.avd/sdcard.img
cp sdcard.img.qcow2 ~/.android/avd/Pixel_3a_API_30.avd/sdcard.img.qcow2


# Install the required tools and the emulator itself
- sdkmanager "platform-tools" "platforms;android-30" "build-tools;30.0.3" "emulator"
# Install system images for the emulator
- sdkmanager "system-images;android-30;google_apis;x86"
# Create an emulator with the installed system images
- echo no | avdmanager create avd -n test-emulator -k "system-images;android-30;google_apis;x86"
# Start the emulator with no audio, boot animation, window, and with GPU acceleration off
- emulator -avd test-emulator -noaudio -no-boot-anim -gpu off -no-window &
# Wait for the emulator to boot completely
- adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;'
# Dismiss the emulator lock screen and wait 1 second for it to settle
- adb shell wm dismiss-keyguard
- sleep 1
# Disable window and transition animations. This is required to run UI tests correctly
- adb shell settings put global window_animation_scale 0
- adb shell settings put global transition_animation_scale 0
- adb shell settings put global animator_duration_scale 0
# Configuration to be able to do Screenshot Test on API higher that 27
- adb shell settings put global hidden_api_policy_p_apps 1
- adb shell settings put global hidden_api_policy_pre_p_apps 1
- adb shell settings put global hidden_api_policy  1