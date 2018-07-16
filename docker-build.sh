#!/usr/bin/env bash

set -ex

FILE=docker/Dockerfile
IMAGE=jchipmunk/strelastik:0.0.1

docker build \
  --file=${FILE} \
  -t ${IMAGE} \
  --no-cache \
  .

docker inspect ${IMAGE}