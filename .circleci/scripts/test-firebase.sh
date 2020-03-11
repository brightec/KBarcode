#!/bin/bash

# Name variables
PROJECT_NAME=$1
BUILD_NO=$2
TEST_DIR=$3
RESULTS_BUCKET=$4

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

# Install Flank
wget --quiet https://github.com/TestArmada/flank/releases/download/v8.1.0/flank.jar -O ./flank.jar

# Get the apk path
APK_PATH=$(find . -path "*.apk" ! -path "*unaligned.apk" ! -path "*Test*.apk" -print -quit)

# Get the app test apk path
APK_APP_TEST_PATH=$(find . -path "*app*Test*.apk" -print -quit)

# Setup GCloud Auth
echo "$GCP_SERVICE_KEY" > "$HOME/gcp-service-key.json"
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/gcp-service-key.json"

# Create Flank config file
cat <<EOF > "$HOME/flank.yml"
gcloud:
  app: $APK_PATH
  test: $APK_APP_TEST_PATH
  type: instrumentation
  device:
    - model: NexusLowRes
      version: 28
      locale: en
      orientation: portrait
  timeout: 30m
  results-bucket: $RESULTS_BUCKET
  results-dir: $PROJECT_NAME/$BUILD_NO
flank:
  max-test-shards: -1
  shard-time: 120
  smart-flank-gcs-path: gs://$RESULTS_BUCKET/$PROJECT_NAME/flank/android.xml
  project: $FB_TEST_LAB_PROJECT_ID
  local-result-dir: $TEST_DIR
  files-to-download:
    - .*\.mp4
    - .*\.xml
EOF

count=$(find . -path "*Test*.apk" ! -path "*app*Test*.apk" | wc -l)
# shellcheck disable=SC2004
if (( $count > 0 )); then
  echo "  additional-app-test-apks:" >> "$HOME/flank.yml"

  # Loop through all additional test apks and append them to Flank config
  while IFS= read -r -d '' TEST_APK_PATH; do
    echo "    - test: $TEST_APK_PATH" >> "$HOME/flank.yml"
  done < <(find . -path "*Test*.apk" ! -path "*app*Test*.apk" -print0)
fi

# Run Flank
java -jar ./flank.jar android run --config="$HOME/flank.yml"
