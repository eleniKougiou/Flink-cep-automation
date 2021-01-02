# Flink-cep-automation

Using Apache [Flink CEP](https://github.com/apache/flink) to create an automatic pattern generator.

In CEPCase_Generate the input sequence (input) and the event pattern (wanted), based on different contiguity conditions and after match skip strategies,
are created (createInput, createPattern). Then the matching results are produced (createResult). 

All the above processes are performed automatically with the help of class Generate.
