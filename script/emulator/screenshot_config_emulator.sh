#!/bin/bash


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Specific configuration to avoid Facebook Screenshot library to fail on API devices higher than 27"
echo $DIR
adb shell settings put global hidden_api_policy_p_apps 1
adb shell settings put global hidden_api_policy_pre_p_apps 1
adb shell settings put global hidden_api_policy  1