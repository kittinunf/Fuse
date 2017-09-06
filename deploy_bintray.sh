#!/bin/bash

if [[ "$TRAVIS_BRANCH" == */release-v* ]]; then

  echo "We're on release branch, deploying"

  modules=("fuse")

  for i in "${modules[@]}"
  do
    ./gradlew :$i:clean :$i:build :$i:bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false -x mavenAndroidJavadocs
  done

fi