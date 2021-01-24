#!/bin/bash

cd ..

#Start Cluster
./bin/start-cluster.sh

# Submit job
./bin/flink run ./target/flinkCEP-GeneratePatterns-0.1-jar-with-dependencies.jar "$@"

# Stop the cluster again
./bin/stop-cluster.sh
