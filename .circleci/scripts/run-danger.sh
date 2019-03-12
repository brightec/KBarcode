#!/bin/bash

# Install and run danger from .circleci/danger folder
cd .circleci/danger

# Install and run danger
yarn add danger
yarn danger ci