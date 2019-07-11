#!/bin/bash

set -e
apt-get update
apt-get -y install \
    libxslt1-dev \
    openssl
apt-get clean
rm -rf /var/lib/apt/lists/*
