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
package flinkCEP.events;

import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;


public class Generate implements Serializable{

    static String strP; // wanted pattern
    static int n = 0; // number of events in the pattern
    static int nr = 0; // number of results
    static double start = System.currentTimeMillis();
    static StreamExecutionEnvironment env;

    static int contiguity = 2;
    // 1 = strict, 2 = relaxed, 3 = non-deterministic relaxed

    int strategy = 1;
    // 1 = no skip, 2 = skip to next, 3 = skip past last event, 4 = skip to first, 5 = skip to last

    static AfterMatchSkipStrategy skipStrategy;
    String patternName;
    static String inputStr;
    static boolean first = true;
    static int nArgs = 0;

    public Generate(String strP, int contiguity, StreamExecutionEnvironment env, int nArgs){
        this.strP = strP.toLowerCase(Locale.ROOT).replaceAll("\\s+","");
        this.contiguity = contiguity;
        this.env = env;
        this.nArgs = nArgs;
    }

    public void setStrP(String strP) {
        this.strP = strP.toLowerCase(Locale.ROOT).replaceAll("\\s+","");
    }

    public static String getStrP() {
        return strP;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public static int getNr() {
        return nr;
    }

    public static double getTime() {
        return System.currentTimeMillis() - start;
    }

    public void setContiguity(int contiguity) {
        this.contiguity = contiguity;
    }

    public static String getContiguity() {
        String cont = "";
        switch (contiguity){
            case 1: // Strict contiguity
                cont =  "Strict Contiguity";
                break;
            case 2: // Relaxed contiguity
                cont =  "Relaxed Contiguity";
                break;
            case 3: // Non - deterministic relaxed contiguity
                cont =  "Non - deterministic relaxed Contiguity";
                break;
        }
        return cont;
    }

    public void setStrategy(int strategy, String patternName) {
        this.strategy = strategy;
        this.patternName = patternName;
        switch (strategy) {
            case 1: // No skip strategy
                skipStrategy = AfterMatchSkipStrategy.noSkip();
                break;
            case 2: // Skip to next strategy
                skipStrategy = AfterMatchSkipStrategy.skipToNext();
                break;
            case 3: // Skip past last event strategy
                skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent();
                break;
            case 4: // Skip to first strategy
                skipStrategy = AfterMatchSkipStrategy.skipToFirst(patternName);
                break;
            case 5: // Skip to last strategy
                skipStrategy = AfterMatchSkipStrategy.skipToLast(patternName);
                break;
        }
    }

    public String getStrategy() {
        String retStrategy = "";
        switch (strategy){
            case 1: // No skip strategy
                retStrategy =  "No skip strategy";
                break;
            case 2: // Skip to next strategy
                retStrategy =  "Skip to next strategy";
                break;
            case 3: // Skip past last event strategy
                retStrategy =  "Skip past last event strategy";
                break;
            case 4: // Skip to first strategy
                retStrategy =  "Skip to first strategy";
                break;
            case 5: // Skip to last strategy
                retStrategy =  "Skip to last strategy";
                break;
        }
        return retStrategy;
    }

    public void setN(int n) {
        this.n = n;
    }

    public static int getN() {
        return n;
    }

    public static Pattern<Event, ?> createPattern() throws Exception {
        Pattern<Event, ?> resultP = null;
        char cur;
        String curStr, nextStr;

        for (int i = 0; i < strP.length(); i++){
            cur = strP.charAt(i); // for each character of the sequence
            if(Character.isLetter(cur)){
                curStr = Character.toString(cur);
                if ((i + 1) < strP.length() && strP.charAt(i + 1) == '|'){ // letter OR letter
                    i += 2;
                    nextStr = Character.toString(strP.charAt(i));
                    resultP = createOr(resultP, curStr, nextStr);
                }else{ // letter
                    resultP = createLetter(resultP, curStr);
                }
            }else if (cur == '+' || cur == '*'){ // + (one or more), * (zero or more)
                switch (contiguity){
                    case 1: // Strict contiguity
                        resultP = resultP.oneOrMore().consecutive();
                        break;
                    case 2: // Relaxed contiguity
                        resultP = resultP.oneOrMore();
                        break;
                    case 3: // Non - deterministic contiguity
                        resultP = resultP.oneOrMore().allowCombinations();
                        break;
                }
                if (cur == '*'){
                    resultP = resultP.optional();
                }
            }else if (cur == '?'){ // ? (optional)
                resultP = resultP.optional();
            }else if(cur == '{') { // {low,high} (we want previous character from low to high times)
                int low = Character.getNumericValue(strP.charAt(i + 1));
                int high = Character.getNumericValue(strP.charAt(i + 3));
                resultP = resultP.times(low, high);
                i += 4; // go to }
            }
        }
        return resultP;
    }

    public static Pattern<Event, ?> createLetter(Pattern<Event, ?> p, String ev){
        // Create wanted condition
        SimpleCondition<Event> cond = new SimpleCondition<Event>() {
            @Override
            public boolean filter(Event value) throws Exception {
                return value.getValue().toLowerCase(Locale.ROOT).startsWith(ev);
            }
        };

        n++; // increase number of events in the pattern
        String strN = Integer.toString(n); // Name of this event in the pattern

        if(n == 1){ // first event
            // "begin" uses non-deterministic contiguity, that is, it will make matches starting with as many alternatives as possible
            p = Pattern.<Event>begin(strN, skipStrategy).where(cond);
        }else{
            switch (contiguity){
                case 1: // Strict contiguity
                    p = p.next(strN).where(cond);
                    break;
                case 2: // Relaxed contiguity
                    p = p.followedBy(strN).where(cond);
                    break;
                case 3: // Non - deterministic contiguity
                    p = p.followedByAny(strN).where(cond);
                    break;
            }
        }
        return p;
    }

    public static Pattern<Event, ?> createOr(Pattern<Event, ?> p, String ev, String evNext){
        // Create wanted condition
        SimpleCondition<Event> cond = new SimpleCondition<Event>() {
            @Override
            public boolean filter(Event value) throws Exception {
                return value.getValue().toLowerCase(Locale.ROOT).startsWith(ev);
            }
        };

        n ++; // increase number of events in the pattern
        String strN = Integer.toString(n); // Name of this event in the pattern

        if(n == 1){ // first
            // "begin" uses non-deterministic contiguity, that is, it will make matches starting with as many alternatives as possible
            p = Pattern.<Event>begin(strN, skipStrategy).where(cond).or(new SimpleCondition<Event>() {
                @Override
                public boolean filter(Event value) throws Exception {
                    return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                }
            });
        }else{
            switch (contiguity){
                case 1: // Strict contiguity
                    p = p.next(strN).where(cond).or(new SimpleCondition<Event>() {
                        @Override
                        public boolean filter(Event value) throws Exception {
                            return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                        }
                    });
                    break;
                case 2: // Relaxed contiguity
                    p = p.followedBy(strN).where(cond).or(new SimpleCondition<Event>() {
                        @Override
                        public boolean filter(Event value) throws Exception {
                            return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                        }
                    });
                    break;
                case 3: // Non - deterministic contiguity
                    p = p.followedByAny(strN).where(cond).or(new SimpleCondition<Event>() {
                        @Override
                        public boolean filter(Event value) throws Exception {
                            return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                        }
                    });
                    break;
            }
        }
        return p;
    }

    public static DataStream<String> createResult(PatternStream<Event> patternStream){
        DataStream<String> result =  patternStream.select((Map<String, List<Event>> p) -> {
            String strResult = "";

            int test;
            // In submitflink the n has not been updated 
            if (n > 0) {
                test = n;
            } else{
                test = 10;
            }
            nr++;
            for (int i = 0; i < test; i++){
                String strN = Integer.toString(i + 1);
                // Check null because the optional() quantifier can be used
                if (p.get(strN) != null) {
                    for (int j = 0; j < p.get(strN).size(); j++) { // for looping patterns (+, *)
                        strResult += p.get(strN).get(j).getValue() + " ";
                    }
                }
            }
            if (first){ // Print time only the first time (all matches have already been found)
                strResult += " (" + getTime() / 1000 + " sec)";
                first = false;
            }
            return nr + ". " + strResult;
        });
        return result;
    }

    @Override
    public String toString() {
        return "Wanted pattern: " + strP + " (Number of events: " + n + ")"
                + "\nContiguity condition: " + this.getContiguity() + "\nAfter match strategy: "
                + this.getStrategy() + "\nNumber of Arguments: " + nArgs + "\n\nMatching results:";
    }

    public static String readFile(String pathName){
        try {
            File file = new File(pathName);
            Scanner myReader = new Scanner(file);
            String data = "";
            while (myReader.hasNextLine()) {
                data += myReader.nextLine();
            }
            myReader.close();
            return data;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    public static Collection<Event> createInput(String pathName) throws Exception {
        Collection<Event> events = new ArrayList<>();
        inputStr = readFile(pathName);
        String[] words = inputStr.split(",");

        for (int i = 0; i < words.length; i++){
            //events.add(new Event(Integer.parseInt(Character.toString(inputStr.charAt(i))), Integer.parseInt(Character.toString(inputStr.charAt(i + 2))), Character.toString(inputStr.charAt(i + 4))));
            events.add(new Event(Integer.parseInt(words[i]), Integer.parseInt(words[i + 1]), words[i + 2]));
            i += 2;
            //events.add(new Event(0, 0, Character.toString(inputStr.charAt(i))));
        }

        return events;
    }
}