#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if ! emulator -list-avds | grep -q Emulator; then
    echo "No emulator for screenshot tests found, creating one..."
    $DIR/create_emulator.sh
fi

if adb devices -l | grep -q emulator; then
    echo "Emulator already running"
    exit 0
fi

$DIR/configure_dpi.sh
echo "Starting emulator..."
echo "no" | emulator -avd Emulator -no-audio -no-boot-anim -verbose -no-snapshot -gpu swiftshader_indirect -skin 1080x2220
$DIR/disable_animations.sh
$DIR/screenshot_config_emulator.sh
$DIR/wait_for_emulator.sh
