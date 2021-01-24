#!/bin/bash

# Exit current folder, go one directory up (Flink-cep-automation).
# There you can find the pom.xml in order to compile the project.
cd .. 

# Clean & compile maven project, rebuild jar file
mvn clean compile assembly:single
