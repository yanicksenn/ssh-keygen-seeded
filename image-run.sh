#!/usr/bin/env zsh

cd "$(dirname "$0")"

docker stop test
docker rm test

docker run -d -it --name test ssh-test:1.0