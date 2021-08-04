#!/bin/bash

echo "Waiting until emulator is completely booted"
while [ "`adb shell getprop sys.boot_completed | tr -d '\r' `" != "1" ] ; do sleep 1; done
echo "Emulator is ready"
