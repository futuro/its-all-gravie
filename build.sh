#!/bin/bash

echo "Beginning build"

# This will auto-succeed in the CI environment, but I've kept it here in case others come to use the
# script
echo "installing asdf java plugin"
asdf plugin add java https://github.com/halcyon/asdf-java.git

echo "Installing local versions"
asdf install

echo "Building release"
npx shadow-cljs release :client
