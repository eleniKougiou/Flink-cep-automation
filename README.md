# Flink-cep-automation

### Using Apache [Flink CEP](https://github.com/apache/flink) to automatically produce patterns and their matching results.

- StreamGenerator.py creates a sequence based on the the requested pattern and conditions we want to examine.

- In Generate.java methods have been developed in order to read the input sequence from the file produced above (createInput), to create the wanted pattern (createPattern) based on specific conditions (contiguity & after match strategy) and to produce the matching results (createResult).

  CEPCase_Generate.java is the one that uses the above methods properly and creates the results in an output file, in order to be able to draw conclusions about different conditions.
 
- "experiments" folder contains shell scripts to experiment with different conditions and study the results. You can find more details there and try it yourself!
