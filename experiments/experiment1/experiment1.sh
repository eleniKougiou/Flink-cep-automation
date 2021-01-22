#!/bin/bash

# Exit current folder, go one directory up (experiments).
# There you can run.sh
cd .. 

# pattern = aaabbb 
# stream length = WANTED, number of matches = 1 
# contiguity = 1 (strict), strategy = 1 (no skip), parallelism = 1
# input file = "seqWANTED.txt", output file = "resultWANTED.txt"
# path to save generated files = $PWD (Print Working Dir)

# stream length from 10000 to 100000 by 10000
for (( i=1; i<=10; i++ ))
do	
	WANTED=$(( i * 10000 ))
	echo "Stream length: $WANTED"
	./run.sh 'aaabbb' $WANTED 1 1 1 1 "seq"$WANTED".txt" "result"$WANTED".txt" $PWD
done
