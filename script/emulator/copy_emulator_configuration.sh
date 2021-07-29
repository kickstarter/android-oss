#!/bin/bash

echo "Copy configuration to the emulator the one specified by us with concrete dimensions WxH and density"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $DIR
cp $DIR/config.ini ~/.android/avd/Pixel_3a_API_30.avd/config.ini
cat ~/.android/avd/Pixel_3a_API_30.avd/config.ini
# Configuration to be able to do Screenshot Test on API higher that 27

