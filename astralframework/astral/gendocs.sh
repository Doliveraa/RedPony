#!/bin/bash

documentation build ../API/index.js -f html -o ../html
documentation readme ../API/index.js --readme-file=../astral/README.md --section=API
