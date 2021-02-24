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
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import flinkCEP.events.Event;
import flinkCEP.events.Generate;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

import static org.apache.flink.core.fs.FileSystem.WriteMode.OVERWRITE;

// Automatic pattern generation and processing
public class CEPCase_Generate {

    public static void main (String[] args) throws Exception {

        String inputFile = "data.txt";
        String outputFile = "out.txt";
        String type = "Kafka";
        int parallelism = 4;
        int contiguity = 2;
        int strategy = 1;
        String wantedStr = "ab+c";

        /*String wantedStr = args[0];
        int contiguity = Integer.parseInt(args[1]);
        int strategy = Integer.parseInt(args[2]);
        int parallelism = Integer.parseInt(args[3]);
        String inputFile = args[4]; // path for seq.txt
        String outputFile = args[5]; // path for result.txt
        String type = args[6];*/

        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Set parallelism to 1
        env.setParallelism(parallelism);

        // Create input sequence
        DataStream<Event> input;
        if(type.equals("Kafka")) {
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", "localhost:9092");
            properties.setProperty("group.id", "test");

            DataStream<String> stream = env
                    .addSource(new FlinkKafkaConsumer<>("CEPdata", new SimpleStringSchema(), properties)/*.setStartFromEarliest()*/);

            input = stream.map(new MapFunction<String, Event>() {

                @Override
                public Event map(String s) throws Exception {

                    String[] words = s.split(",");

                    if(words[2].equals("KILL"))
                        throw new Exception();

                    return new Event(Integer.parseInt(words[0]), Integer.parseInt(words[1]), words[2]);
                }
            });}
        else{
            input = env.fromCollection(Generate.createInput(inputFile));
        }
        //input.print();

        // Set wanted pattern and contiguity condition
        // (1 = strict, 2 = relaxed, 3 = non deterministic relaxed)
        Generate wanted = new Generate(wantedStr, contiguity, env);

        // Set after match skip strategy
        // (1 = no skip, 2 = skip to next, 3 = skip past last event, 4 = skip to first, 5 = skip to last)
        wanted.setStrategy(strategy, ""); // no skip

        // Create wanted pattern
        Pattern<Event, ?> pattern = wanted.createPattern();

        DataStream<String> info = env.fromElements(wanted.toString());

        PatternStream<Event> patternStream = CEP.pattern(input.keyBy("stream_id", "window_id"), pattern);

        // Create result with matches
        DataStream<String> result = wanted.createResult(patternStream);

        // Print and write to file
        DataStream<String> all = info.union(result);
        if(type.equals("Kafka")){
            all.print();
            KafkaSink kp = new KafkaSink("localhost:9092", "CEPout");
            //all.writeAsText(outputFile, OVERWRITE);
            all.addSink(kp.getProducer());
        }else {
            all.print();
            all.writeAsText(outputFile, OVERWRITE);
        }

        env.execute("Flink CEP Pattern Detection Automation");
    }
}