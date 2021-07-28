#!/bin/bash

echo "android list avd"
if android list avd | grep -q Pixel_3a_API_30; then
    echo "There is an existing an emulator to run screenshot tests"
    exit 0;
fi

echo "Creating a brand new SDCard..."
rm -rf sdcard.img
emulator/mksdcard -l e 1G sdcard.img
echo "SDCard created!"

echo "Downloading the image to create the emulator..."
echo no | sdkmanager "system-images;android-30;google_apis;x86"
echo "Image downloaded!"

echo "Creating the emulator to run screenshot tests..."
echo no | avdmanager create avd -n Pixel_3a_API_30 -k "system-images;android-30;google_apis;x86" --force --sdcard sdcard.img
echo "Emulator created!"


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cp $DIR/config.ini ~/.android/avd/Pixel_3a_API_30.avd/config.ini
cp sdcard.img ~/.android/avd/Pixel_3a_API_30.avd/sdcard.img
cp sdcard.img.qcow2 ~/.android/avd/Pixel_3a_API_30.avd/sdcard.img.qcow2
