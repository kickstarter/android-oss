#!/bin/bash -e

# Move config files into place
cd config
for src in *.example
do
  dest=`basename $src .example`
  test -e $dest || cp $src $dest
done

VARIANTS="externalDebug externalRelease internalDebug internalRelease regressionDebug regressionRelease"

cd ..

# Copy google services over. Fallback to example if they don't exist.
for v in $VARIANTS
do
  test -d app/src/"$v" ||  mkdir -p app/src/"$v"/
  cp vendor/native-secrets/android/"$v"/google-services.json app/src/"$v"/google-services.json || cp config/"$v"/google-services.example.json app/src/"$v"/google-services.json
done
