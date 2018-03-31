#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"

cd $SCRIPT_DIR/../API
sudo forever stopall
