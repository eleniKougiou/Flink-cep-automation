#!/bin/bash

# Terminal: ./runflink.sh arg1 arg2 arg3 arg4 arg5 arg6
# arg1 = compile? , arg2 = pattern, arg3 = contiguity, arg4 = strategy
# arg5 = input file, arg6 = output file

# Stop script if any simple command fails
set -e 

cd flinkCEP-GeneratePatterns

# If first argument ($1) is equal to 1 then compile
if [ $1 -eq 1 ]; then
	# Clean & compile project 
	mvn clean compile
fi

# Load arguments
export JAVA_PROGRAM_ARGS=`echo "$@"` 

# Run project
mvn exec:java -Dexec.mainClass=flinkCEP.cases.CEPCase_Generate -Dexec.args="$JAVA_PROGRAM_ARGS"

cd
