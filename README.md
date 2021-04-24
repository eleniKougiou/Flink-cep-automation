# Flink CEP Automation
## AUEB | Bachelor Thesis (w / Prof. Kotidis Yannis) | 2020 - 2021

The purpose of the project is to create a generalized Complex Event Processing Operator using the library [FlinkCEP](https://ci.apache.org/projects/flink/flink-docs-stable/dev/libs/cep.html). 

### Contributors:
- Kontaxakis Antonios 
- Kotidis Yannis
- Deligiannakis Antonios
 
 ---
 ## How to use:
   
Step 1:  
[StreamGenerator.py](https://github.com/eleniKougiou/Flink-cep-automation/blob/master/Useful%20Files/StreamGenerator.py) creates the text file with the data, based on the requested pattern and conditions we want to examine. The data contains one event per line in the format ``stream_id, window_id, event`` , while the last line of the file is "-1, -1, KILL" in order to understand that there are no other events and to terminate the flink job. The user needs to enter 7 command-line arguments as follows:  
1. pattern (String)
2. stream length (int)
3. number of sub-streams (int)
4. window size (int)
5. number of matches (int)
6. strict contiguity (boolean)
7. file name to write the data (String) 

#### Example 
    ./StreamGenerator_new.py 'ab+(c|d)' 1000 8 100 150 True 'data.txt'


- In Generate.java methods have been developed in order to read the input sequence from the file produced above (createInput), to create the wanted pattern (createPattern) based on specific conditions (contiguity & after match strategy) and to produce the matching results (createResult).

  CEPCase_Generate.java is the one that uses the above methods properly and creates the results in an output file, in order to be able to draw conclusions about different conditions.
 
- "experiments" folder contains shell scripts to experiment with different conditions and study the results. You can find more details there and try it yourself!
