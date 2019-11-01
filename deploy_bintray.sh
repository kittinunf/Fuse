#!/bin/bash

if [[ "$TRAVIS_BRANCH" == */release-v* ]]; then

  echo "We're on release branch, deploying"

  modules=("fuse")

  for i in "${modules[@]}"
  do
    ./gradlew :$i:clean :$i:build :$i:bintrayUpload -PBINTRAY_USER=$BINTRAY_USER -PBINTRAY_KEY=$BINTRAY_KEY -PdryRun=false
  done

fi