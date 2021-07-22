#!/bin/bash


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#echo "Enabling read and write external storage permissions before running tests"
#$ANDROID_HOME/platform-tools/adb wait-for-device
#$ANDROID_HOME/platform-tools/adb shell pm grant com.kickstarter.internal.debug.test android.permission.WRITE_EXTERNAL_STORAGE
#$ANDROID_HOME/platform-tools/adb shell pm grant com.kickstarter.internal.debug.test android.permission.READ_EXTERNAL_STORAGE

echo "Specific configuration to avoid Facebook Screenshot library to fail on API higher than 27"
$ANDROID_HOME/platform-tools/adb wait-for-device
$ANDROID_HOME/platform-tools/adb shell settings put global hidden_api_policy_p_apps 1
$ANDROID_HOME/platform-tools/adb shell settings put global hidden_api_policy_pre_p_apps 1
$ANDROID_HOME/platform-tools/adb shell settings put global hidden_api_policy  1

echo "Rebooting emulator to apply the configuration"
$ANDROID_HOME/platform-tools/adb wait-for-device
$DIR/reboot_emulator.sh