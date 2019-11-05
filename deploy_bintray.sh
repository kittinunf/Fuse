#!/bin/bash

if [[ "$TRAVIS_BRANCH" == */release-v* ]]; then

  echo "We're on release branch, deploying"

  for i in $(ls -d */);
  do
    m=${i%%/}
    if [[ $m == fuse* ]]; then
      echo ">> Deploying $m ..."
      ./gradlew :$i:clean :$i:build :$i:bintrayUpload -PBINTRAY_USER=$BINTRAY_USER -PBINTRAY_KEY=$BINTRAY_KEY -PdryRun=false
    fi
  done

fi