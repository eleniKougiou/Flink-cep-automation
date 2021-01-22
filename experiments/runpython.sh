#!/bin/bash

# Exit current folder, go one directory up (Flink-cep-automation).
# There you can find StreamGenerator.py
cd .. 

# Terminal: ./runpython.sh arg1 arg2 arg3 arg4 arg5 arg6
# arg1 = pattern, arg2 = stream length, arg3 = number of matches, arg4 = contiguity, arg5 = output file, arg6 = number of experiment

python3 StreamGenerator.py "$@"
