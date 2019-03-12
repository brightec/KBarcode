#!/bin/bash

# Safety checks
if [ -z "$GCP_SERVICE_KEY" ]; then
  echo "GCP_SERVICE_KEY env variable is empty. Exiting."
  exit 1
fi
if [ -z "$FB_TEST_LAB_PROJECT_ID" ]; then
  echo "FB_TEST_LAB_PROJECT_ID env variable is empty. Exiting."
  exit 1
fi

# Export to secrets file
echo $GCP_SERVICE_KEY > $HOME/gcp-service-key.json

# Set project ID
gcloud config set project $FB_TEST_LAB_PROJECT_ID

# Auth account
gcloud auth activate-service-account --key-file $HOME/gcp-service-key.json

# Delete secret
rm $HOME/gcp-service-key.json
