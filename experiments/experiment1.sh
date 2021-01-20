#!/bin/bash

# Stop script if any simple command fails
set -e

# Same in all: 
# pattern = aaabbb, number of matches = 1, contiguity = strict, strategy = no skip

# Different:
# stream length from 10000
for (( i=1; i<=10; i++ ))
do	
	wanted=$(( i * 10000 ))
	echo "Stream length: $wanted"
	./run.sh 'aaabbb' $wanted 1 1 1 "seq"$wanted".txt" "result"$wanted".txt" 
done
