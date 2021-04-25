/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flinkCEP.cases;
import flinkCEP.KafkaSink;
import flinkCEP.events.Event;
import flinkCEP.events.Generate;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

import static org.apache.flink.core.fs.FileSystem.WriteMode.OVERWRITE;

//Automatic pattern generation and processing
public class CEPCase_Generate {

    private static String jobName, inputFile, outputFile, type, wantedStr, patternName;
    private static String topicIn, topicOut, host;
    private static int parallelism, contiguity, strategy;

   public static void main (String[] args) throws Exception {

       if(args.length == 12){
           givenArgs(args);
       }else {
           defaultArgs();
       }

       // Set up the execution environment
       StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
       // Set parallelism
       env.setParallelism(parallelism);

       DataStream<Event> input;

       // Create input sequence
       if(type.equals("Kafka")) {
           // Read data from Kafka topic
           Properties properties = new Properties();
           properties.setProperty("bootstrap.servers", host + ":9092");
           properties.setProperty("group.id", "test");

           DataStream<String> stream = env
                   .addSource(new FlinkKafkaConsumer<>(topicIn, new SimpleStringSchema(), properties));

            input = stream.map(new MapFunction<String, Event>() {

               @Override
               public Event map(String s) throws Exception {

                   String[] words = s.split(",");

                   //
                   if(words[2].equals("KILL")){
                       throw new Exception();
                   }

                   return new Event(Integer.parseInt(words[0]), Integer.parseInt(words[1]), words[2]);
               }
           });}

       else{
           // Read data from text file
          input = env.fromCollection(Generate.createInput(inputFile));
       }

       // Set wanted pattern and contiguity condition (1 = strict, 2 = relaxed, 3 = non deterministic relaxed)
       Generate wanted = new Generate(wantedStr, contiguity);

       // Set after match skip strategy
       // (1 = no skip, 2 = skip to next, 3 = skip past last event, 4 = skip to first, 5 = skip to last)
       wanted.setStrategy(strategy, patternName);

       // Create wanted pattern
       Pattern<Event, ?> pattern = wanted.createPattern();

       // Print details
       DataStream<String> info = env.fromElements(wanted.toString());

       PatternStream<Event> patternStream = CEP.pattern(input.keyBy("stream_id", "window_id"), pattern);

       // Create result with matches
       DataStream<String> result = wanted.createResult(patternStream);

       DataStream<String> all = info.union(result);

       if(type.equals("Kafka")){
           // Write results to Kafka topic
           KafkaSink kp = new KafkaSink(host + ":9092", topicOut);
           all.addSink(kp.getProducer());
       }else {
           // Write results to file
           all.print();
           all.writeAsText(outputFile, OVERWRITE);
       }
       env.execute(jobName);
   }

   // Default values
    private static void defaultArgs() {
        inputFile = "here.txt";
        outputFile = "results.txt";
        type = "Kafka";
        wantedStr = "ab+c";
        parallelism = 4;
        contiguity = 2;
        strategy = 2;
        patternName = "1";
        jobName = "Flink CEP Pattern Detection Automation";
        topicIn = "CEPdata";
        topicOut = "CEPout";
        host = "localhost";
    }

    // Wanted values
    private static void givenArgs(String[] args){
        type = args[0];
        inputFile = args[1];
        outputFile = args[2];
        wantedStr = args[3];
        parallelism = Integer.parseInt(args[4]);
        contiguity = Integer.parseInt(args[5]);
        strategy = Integer.parseInt(args[6]);
        patternName = args[7];
        jobName = args[8];
        topicIn = args[9];
        topicOut = args[10];
        host = args[11];
    }
}
