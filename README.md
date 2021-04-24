# Flink CEP Automation
## AUEB | Bachelor Thesis (w / Prof. Kotidis Yannis) | 2020 - 2021

The purpose of the project is to create a generalized Complex Event Processing Operator using the library [FlinkCEP](https://ci.apache.org/projects/flink/flink-docs-stable/dev/libs/cep.html). 

### Contributors:
- Kontaxakis Antonios 
- Kotidis Yannis
 
 ---
 ## How to use:
   
### Step 1:  
[StreamGenerator.py](https://github.com/eleniKougiou/Flink-cep-automation/blob/master/Useful%20Files/StreamGenerator.py) creates the text file with the data, based on the requested pattern and conditions we want to examine. The data contains one event per line in the format ``stream_id, window_id, event`` , while the last line of the file is "-1, -1, KILL" in order to understand that there are no other events and to terminate the flink job. The user needs to enter 7 command-line arguments as follows:  
1. Pattern (String)
2. Stream length (int)
3. Number of sub-streams (int)
4. Window size (int)
5. Number of matches (int)
6. Strict contiguity (boolean)
7. File name for writing data (String) 

#### Command example 
    ./StreamGenerator.py 'ab{1,3}(c|d)' 1000 8 100 150 True 'data.txt'
  
### Step 2:
[CEPdata.java](https://github.com/eleniKougiou/Flink-cep-automation/blob/master/Data%20Kafka/src/main/java/CEPdata.java) sends the data to a Kafka topic. The user needs to enter 3 command-line arguments as follows:  
1. File name for reading data (String)
2. Name of the Kafka topic for sending data (String)
3. Host IP (String)

#### Command example (with jar)
    java -jar data_kafka.jar 'data.txt' 'CEPdata' '1.2.3.4'


### Step 3:
[CEPCase_Generate.java](https://github.com/eleniKougiou/Flink-cep-automation/blob/master/src/main/java/flinkCEP/cases/CEPCase_Generate.java) contains all the important operations:
- Reads the data from a Kafka topic (or from a text file)
- Re-writes the wanted regular expression to a FlinkCEP pattern based on wanted conditions
- Finds the matching results
- Writes the results to a Kafka topic (or to a text file)  
  
The user needs to enter 12 command-line arguments as follows: 
1. Type (String): "Kafka" for using Kafka topics to read and write, or anything else for using text files.
2. File name for reading data (String) (useful when type != "Kafka")
3. File name for writing results (String) (useful when type != "Kafka")
4. Pattern (String)
5. Parallelism (int)
6. Contiguity Condition (int): 1 = strict, 2 = relaxed, 3 = non deterministic relaxed
7. After match skip strategy (int): 1 = no skip, 2 = skip to next, 3 = skip past last event, 4 = skip to first, 5 = skip to last
8. Pattern name (int) (useful when strategy = 4 or strategy = 5)
9. Flink job name (String)
10. Name of the Kafka topic for reading data (String) (useful when type = "Kafka")
11. Name of the Kafka topic for writing results (String) (useful when type = "Kafka")
12. Host IP (String)



#### Command example (with jar, submitting job to a Flink cluster)
    ./bin/flink run ./examples/flink_job.jar 'Kafka' '-' '-' 'ab{1,3}(c|d)' 4 1 3 '-' 'Example' 'CEPdata' 'CEPout' '1.2.3.4'
