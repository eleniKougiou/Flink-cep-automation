#!/bin/bash

# Exit current folder, go one directory up (Flink-cep-automation).
# There you can find necessary project files
cd ..

# Terminal: ./runflink.sh arg1 arg2 arg3 arg4 arg5
# arg1 = pattern, arg2 = contiguity, arg3 = strategy
# arg4 = input file, arg5 = output file

# Load arguments
export JAVA_PROGRAM_ARGS=`echo "$@"` 

# Run project
mvn exec:java -Dexec.mainClass=flinkCEP.cases.CEPCase_Generate -Dexec.args="$JAVA_PROGRAM_ARGS"
