#!/bin/bash

echo "Stopping emulator..."
$ANDROID_HOME/platform-tools/adb emu kill
echo "Emulator stopped!"
