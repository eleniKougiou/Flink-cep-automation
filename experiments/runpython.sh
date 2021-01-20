#!/bin/bash

# Terminal ./runpython.sh arg1 arg2 arg3 arg4 arg5
# arg1 = pattern, arg2 = stream length, arg3 = number of matches, arg4 = contiguity, arg5 =output file

python3 StreamGenerator.py "$@"
