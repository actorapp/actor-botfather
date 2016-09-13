#!/bin/bash

# Building BotFather
./gradlew clean assembleDist

# Unpacking Distrib
cd build
rm -fr docker
mkdir -p docker
cd distributions
rm -fr actor-bots
unzip actor-bots.zip
cp -r actor-bots/* ../docker/
cd ../..

# Building docker
docker build -t actor/bot-father .