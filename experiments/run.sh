#!/bin/bash

# Terminal ./run.sh arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8
# arg1 = pattern, arg2 = stream length, arg3 = number of matches
# arg4 = contiguity, arg5 = strategy, arg6 =input file, arg7 = output file
# arg8 = number of experiment

# Stop script if any simple command fails
set -e 

int='^[0-9]+$' 

echo "(correct format: ./runpython.sh arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8) "
echo ""

if [ "$#" -ne 8 ]; then # Wrong number of parameters
    	echo "ERROR: Wrong number of parameters. "
	echo "arg1 = 'wanted pattern' ('String')"
    	echo "arg2 = stream length (int)"
    	echo "arg3 = number of wanted matches (int)"
    	echo "arg4 = wanted contiguity condition (int)"
    	echo "(1: strict, 2: relaxed, 3: non-deterministic relaxed contiguity)"
    	echo "arg5 = wanted after match strategy (int)"
    	echo "(1: no skip, 2: skip to next, 3: skip past last event, 4: skip to first, 5: skip to last)"
    	echo "arg6 = 'name of input file' ('String')"
    	echo "arg7 = 'name of output file' ('String')"
    	echo "arg8 = number of experiment (int)"

elif [[ $1 =~ $int ]] ; then # arg1 is not a string
   	echo "ERROR: arg1 must be a String (give wanted pattern)"
 
elif ! [[ $2 =~ $int ]] ; then # arg2 is not a number
   	echo "ERROR: arg2 must be an integer (give stream length)"
   	
elif [ $2 -le 0 ]; then # arg2 is <= 0 
	echo "ERROR: arg2 must be greater than 0"
	echo "arg3 = stream length"
	
elif ! [[ $3 =~ $int ]] ; then # arg3 is not a number
   	echo "ERROR: arg3 must be an integer (give number of wanted matches)"
   	
elif [ $3 -le 0 ]; then # arg3 is <= 0 
	echo "ERROR: arg3 must be greater than 0"
	echo "arg3 = number of wanted matches"
	
elif ! [[ $4 =~ $int ]] ; then # arg4 is not a number
   	echo "ERROR: arg4 must be an integer, 1, 2 or 3:" 
   	echo "arg4 = wanted contiguity condition"
   	echo "(1: strict, 2: relaxed, 3: non-deterministic relaxed contiguity)"
   		
elif ! [ $4 -eq 1 -o $4 -eq 2 -o $4 -eq 3 ]; then # arg4 is not 1, 2 or 3
	echo "ERROR: arg4 can only be 1, 2 or 3:"
	echo "arg4 = wanted contiguity condition"
	echo "(1: strict, 2: relaxed, 3: non-deterministic relaxed contiguity)"

elif ! [[ $5 =~ $int ]] ; then # arg5 is not a number
   	echo "ERROR: arg4 must be an integer: 1, 2, 3, 4 or 5:" 
   	echo "arg5 = wanted after match strategy"
    	echo "(1: no skip, 2: skip to next, 3: skip past last event, 4: skip to first, 5: skip to last)"
   	
elif ! [ $5 -eq 1 -o $5 -eq 2 -o $5 -eq 3 -o $5 -eq 4 -o $5 -eq 5 ]; then 
# arg5 is not 1, 2, 3, 4 or 5
	echo "ERROR: arg5 can only be 1, 2, 3, 4 or 5:" 
   	echo "arg5 = wanted after match strategy"
    	echo "(1: no skip, 2: skip to next, 3: skip past last event, 4: skip to first, 5: skip to last)"

elif [[ $6 =~ $int ]] ; then # input file is not a string
   	echo "ERROR: arg6 must be a String (give the name of input file)"
 
elif [[ $7 =~ $int ]] ; then # output file is not a string
   	echo "ERROR: arg7 must be a String (give the name of output file)"

elif ! [[ $8 =~ $int ]] ; then # arg8 is not a number
   	echo "ERROR: arg8 must be an integer (number of experiment)"
   	
elif [ $8 -le 0 ]; then # arg8 is <= 0 
	echo "ERROR: arg8 must be greater than 0"
	echo "arg8 = number of experiment"


else # correct arguments
	# save $6 (seq_.txt) file to the appropriate experiment folder (experiment$8)
	inputPath="$PWD/experiment$8/$6"
	# save $7 (result_.txt) file to the appropriate experiment folder (experiment$8)
	outputPath="$PWD/experiment$8/$7"
	
	echo "RUN STREAM GENERATOR" 
	./runpython.sh $1 $2 $3 $4 $inputPath

	echo "RUN FLINK PROGRAM"
	./runflink.sh $1 $4 $5 $inputPath $outputPath
fi
