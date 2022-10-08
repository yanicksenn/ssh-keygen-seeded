#!/usr/bin/env zsh

cd "$(dirname "$0")"

docker build -t ssh-test:1.0 .