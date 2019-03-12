#!/bin/bash

# Name variables
ARTIFACTS_DIR=$1

# Safety checks
if [ -z "$ARTIFACTS_DIR" ]; then
  echo "ARTIFACTS_DIR variable not supplied. Exiting."
  exit 1
fi

# Find and copy all reports
find . -path "*build/reports" | while read REPORTS_PATH; do
  FOLDER=$(echo $REPORTS_PATH | cut -d "/" -f 2)
  DIR="$ARTIFACTS_DIR/$FOLDER"
  mkdir -p $DIR
  cp -r $REPORTS_PATH $DIR
done

# Find and copy all test results
find . -path "*build/test-results" | while read TEST_RESULTS_PATH; do
  FOLDER=$(echo ${TEST_RESULTS_PATH} | cut -d "/" -f 2)
  DIR="$ARTIFACTS_DIR/$FOLDER"
  mkdir -p $DIR
  cp -r $TEST_RESULTS_PATH $DIR
done
