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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;


public class Generate implements Serializable{

    private static String strP; // Wanted pattern
    private static int n = 0; // Number of events in the pattern
    private static int nr = 0; // Number of results

    // 1 = strict, 2 = relaxed, 3 = non-deterministic relaxed
    private static int contiguity = 2;

    // 1 = no skip, 2 = skip to next, 3 = skip past last event, 4 = skip to first, 5 = skip to last
    private int strategy = 1;
    private static AfterMatchSkipStrategy skipStrategy;

    public Generate(String strP, int contiguity){
        Generate.strP = strP.toLowerCase(Locale.ROOT).replaceAll("\\s+","");
        Generate.contiguity = contiguity;
    }

    public void setStrP(String strP) {
        Generate.strP = strP.toLowerCase(Locale.ROOT).replaceAll("\\s+","");
    }

    public static String getStrP() {
        return strP;
    }

    public void setNr(int nr) {
        Generate.nr = nr;
    }

    public static int getNr() {
        return nr;
    }

    public void setContiguity(int contiguity) {
        Generate.contiguity = contiguity;
    }

    private static String getContiguity() {
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
        // patterName is only used in "skip to first / last" strategies
        this.strategy = strategy;
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

    private String getStrategy() {
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
        Generate.n = n;
    }

    public static int getN() {
        return n;
    }

    // Create wanted pattern with the help of FlinkCEP library
    public static Pattern<Event, ?> createPattern() {
        Pattern<Event, ?> resultP = null;
        char cur;
        String curStr, nextStr;

        for (int i = 0; i < strP.length(); i++){
            // For each character of the sequence
            cur = strP.charAt(i);
            if(Character.isLetter(cur)){
                // Letter
                curStr = Character.toString(cur);
                if ((i + 1) < strP.length() && strP.charAt(i + 1) == '|'){
                    // Letter OR letter
                    i += 2;
                    nextStr = Character.toString(strP.charAt(i));
                    resultP = createOr(resultP, curStr, nextStr);
                }else{
                    // Letter (without OR)
                    resultP = createLetter(resultP, curStr);
                }
            }else if (cur == '+' || cur == '*'){
                // + (one or more) or * (zero or more)
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
            }else if (cur == '?'){
                // ? (optional)
                resultP = resultP.optional();
            }else if(cur == '{') {
                // {low,high} (we want the previous character from low to high times)
                int low = Character.getNumericValue(strP.charAt(i + 1));
                int high = Character.getNumericValue(strP.charAt(i + 3));
                resultP = resultP.times(low, high);
                i += 4; // Go to "}" and get ready for next event
            }
        }
        return resultP;
    }

    // Add a letter to the pattern based on wanted conditions
    private static Pattern<Event, ?> createLetter(Pattern<Event, ?> p, String ev){
        // Create wanted condition
        SimpleCondition<Event> cond = new SimpleCondition<Event>() {
            @Override
            public boolean filter(Event value) {
                return value.getValue().toLowerCase(Locale.ROOT).startsWith(ev);
            }
        };

        n++; // Increase number of events in the pattern
        String strN = Integer.toString(n); // Name of this event in the pattern

        if(n == 1){ // First event
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

    // Create OR between two events based on wanted conditions (OR is only supported between two events)
    private static Pattern<Event, ?> createOr(Pattern<Event, ?> p, String ev, String evNext){
        // Create wanted condition
        SimpleCondition<Event> cond = new SimpleCondition<Event>() {
            @Override
            public boolean filter(Event value) {
                return value.getValue().toLowerCase(Locale.ROOT).startsWith(ev);
            }
        };

        n ++; // Increase number of events in the pattern (consider "event OR event" as one event)
        String strN = Integer.toString(n); // Name of this event in the pattern

        if(n == 1){ // first
            // "begin" uses non-deterministic contiguity, that is, it will make matches starting with as many alternatives as possible
            p = Pattern.<Event>begin(strN, skipStrategy).where(cond).or(new SimpleCondition<Event>() {
                @Override
                public boolean filter(Event value) {
                    return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                }
            });
        }else{
            switch (contiguity){
                case 1: // Strict contiguity
                    p = p.next(strN).where(cond).or(new SimpleCondition<Event>() {
                        @Override
                        public boolean filter(Event value) {
                            return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                        }
                    });
                    break;
                case 2: // Relaxed contiguity
                    p = p.followedBy(strN).where(cond).or(new SimpleCondition<Event>() {
                        @Override
                        public boolean filter(Event value) {
                            return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                        }
                    });
                    break;
                case 3: // Non - deterministic contiguity
                    p = p.followedByAny(strN).where(cond).or(new SimpleCondition<Event>() {
                        @Override
                        public boolean filter(Event value) {
                            return value.getValue().toLowerCase(Locale.ROOT).startsWith(evNext);
                        }
                    });
                    break;
            }
        }
        return p;
    }

    // Create result with the matching results
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
                    for (int j = 0; j < p.get(strN).size(); j++) { // For looping patterns (+, *)
                        strResult += p.get(strN).get(j).getValue() + " ";
                    }
                }
            }

            return nr + ". " + strResult;
        });
        return result;
    }

    @Override
    public String toString() {
        return "Wanted pattern: " + strP + " (Number of events: " + n + ")"
                + "\nContiguity condition: " + getContiguity() + "\nAfter match strategy: "
                + this.getStrategy() + "\n\nMatching results:";
    }

    // Read text file with data
    private static ArrayList<String> readFile(String pathName){
        try {
            ArrayList<String> data = new ArrayList<>();
            File file = new File(pathName);
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String lineStr = myReader.nextLine();
                String[] line;
                line = lineStr.split(",");
                if(line[0] != "-1"){
                    for (int i = 0; i < 3; i++){
                        data.add(line[i].replaceAll("\\s+",""));
                    }
                }
            }
            myReader.close();
            return data;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    // Create input from text file
    public static Collection<Event> createInput(String pathName) {
        Collection<Event> events = new ArrayList<>();
        ArrayList<String> words = readFile(pathName);
        for (int i = 0; i < words.size(); i++){
            events.add(new Event(Integer.parseInt(words.get(i)), Integer.parseInt(words.get(i + 1)), words.get(i + 2)));
            i += 2;
        }
        return events;
    }
}
