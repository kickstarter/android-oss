#!/bin/bash

while [ "`adb shell getprop sys.boot_completed | tr -d '\r' `" != "1" ] ; do sleep 1; done
echo "Done"
echo "Emulator is ready"
