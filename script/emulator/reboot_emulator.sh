#!/bin/bash

echo "Rebooting emulator..."
$ANDROID_HOME/platform-tools/adb -e reboot
$DIR/wait_for_emulator.sh
echo "Emulator rebooted!"