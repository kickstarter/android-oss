#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if android list avd | grep -q Emulator; then
    echo "There is an existing an emulator to run screenshot tests"
    exit 0;
fi

echo "Downloading the image to create the emulator..."
sdkmanager "system-images;android-30;google_apis;x86"
echo "Image downloaded!"

echo "Creating the emulator to run screenshot tests..."
echo no | avdmanager create avd -n Emulator -k "system-images;android-30;google_apis;x86"
echo "Emulator created!"