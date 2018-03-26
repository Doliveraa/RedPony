#!/bin/bash

documentation build ../astral/index.js -f html -o ../html
documentation readme ../astral/index.js --readme-file=../astral/README.md --section=API
