#!/bin/bash

# Name variables
PROJECT_NAME=$1
BUILD_NO=$2
TEST_DIR=$3
RESULTS_BUCKET=$4
NUM_TESTRUNS_FAILED=0

# Safety checks
if [ -z "$PROJECT_NAME" ]; then
  echo "PROJECT_NAME variable not supplied. Exiting."
  exit 1
fi
if [ -z "$BUILD_NO" ]; then
  echo "BUILD_NO variable not supplied. Exiting."
  exit 1
fi
if [ -z "$TEST_DIR" ]; then
  echo "TEST_DIR variable not supplied. Exiting."
  exit 1
fi
if [ -z "$RESULTS_BUCKET" ]; then
  echo "RESULTS_BUCKET variable not supplied. Exiting."
  exit 1
fi

# Runs a set of tests
test_apk() {
  # Set DIR
  RESULTS_DIR=${PROJECT_NAME}_${BUILD_NO}_${1}

  # Run tests
  gcloud firebase test android run \
    --type instrumentation \
    --app $2 \
    --test $3 \
    --device model=Pixel2,version=27,locale=en,orientation=portrait \
    --timeout 30m \
    --results-bucket $RESULTS_BUCKET \
    --results-dir=$RESULTS_DIR

  # Capture exit status of gcloud command
  TEST_RETURN_CODE=$?

  # Make result dir
  mkdir -p "$TEST_DIR/$RESULTS_DIR"

  # Pull down test results
  gsutil -m cp -r -U "gs://$RESULTS_BUCKET/$RESULTS_DIR/*" "$TEST_DIR/$RESULTS_DIR"

  if [ $TEST_RETURN_CODE != 0 ]; then
    return $((NUM_TESTRUNS_FAILED + 1))
  else
    return $((NUM_TESTRUNS_FAILED))
  fi
}

# Fix the crcmod installation
echo "y" | sudo pip uninstall crcmod
sudo pip install -U crcmod

# Get the apk path
APK_PATH=$(find . -path "*.apk" ! -path "*unaligned.apk" ! -path "*Test*.apk" -print -quit)

# Loop through all test apks and run them
while IFS= read -r -d '' TEST_APK_PATH; do
    FOLDER=$(echo $TEST_APK_PATH | cut -d "/" -f 2)
    test_apk \
      $FOLDER \
      $APK_PATH \
      $TEST_APK_PATH
    NUM_TESTRUNS_FAILED=$?
done < <(find . -path "*Test*.apk" -print0)

# Return with the number of failed test runs (zero = success)
exit $NUM_TESTRUNS_FAILED
