#!/bin/bash

echo "Copy configuration to the emulator in order to have a concrete Screen size specified on config.ini"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cp $DIR/config.ini ~/.android/avd/Pixel_3a_API_30.avd/config.ini

