#!/bin/bash


# Create data ########################################

echo "START STREAM GENERATOR"
echo ""

# pattern, stream_length, num_sub_streams, window_size, num_matches, strict, fileName
./StreamGenerator_new.py 'ab{1,3}(c|d)' 1000 8 1000 100 True 'data.txt'

echo "END STREAM GENERATOR"
echo ""

# Send data to Kafka #################################

echo "START CEPDATA"
echo ""

# fileName, topicName, host
java -jar dataKafka_final.jar 'data.txt' 'CEPdata' 'localhost'

echo "END CEPDATA"
echo ""

# Send job to cluster ################################

echo "START FLINK JOB"
echo ""

cd flink
# type, fileIn, fileOut, pattern, parallelism, contiguity, strategy, patternName, jobName, topicNameIn, topicNameOut, host
./bin/flink run ./examples/flink_final.jar Kafka '-' '-' 'ab{1,3}(c|d)' 4 1 3 '-' 'Example' 'CEPdata' 'CEPout' 'localhost'

echo "END FLINK JOB"
echo ""
