#!/bin/bash

echo "Inspecting asdf plugins"
asdf info
asdf plugin list

echo "Beginning build"

echo "installing asdf java plugin"
asdf plugin add java https://github.com/halcyon/asdf-java.git

echo "Installing local versions"
asdf install

# echo "Fetching JDK"
# # Grab the java binary and put it in our path
# wget https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8%2B7/OpenJDK17U-jre_x64_linux_hotspot_17.0.8_7.tar.gz
# # When transform the initial directory name into something static, to make it easier to work with
# tar --gzip --transform 's,^[^/]+,jdk,x' --extract --file OpenJDK17U-jre_x64_linux_hotspot_17.0.8_7.tar.gz
# export PATH=$PWD/jdk/bin:$PATH

echo "Building release"
npx shadow-cljs release :client
