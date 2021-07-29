#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $DIR
cp $DIR/config.ini ~/.android/avd/Pixel_3a_API_30.avd/config.ini
# Configuration to be able to do Screenshot Test on API higher that 27
adb shell settings put global hidden_api_policy_p_apps 1
adb shell settings put global hidden_api_policy_pre_p_apps 1
adb shell settings put global hidden_api_policy  1
