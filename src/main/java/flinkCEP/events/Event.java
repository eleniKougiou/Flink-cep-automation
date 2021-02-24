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

import java.util.Objects;

public class Event{

    private String value;
    private int stream_id, window_id;

    public Event() {}
    public Event(int stream_id, int window_id, String value) {
        this.stream_id = stream_id;
        this.window_id = window_id;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }

    public int getStream_Id() {
        return stream_id;
    }

    public void setStream_id(int stream_id){
        this.stream_id = stream_id;
    }

    public int getWindow_id() {
        return window_id;
    }

    public void setWindow_id(int window_id) {
        this.window_id = window_id;
    }

    @Override
    public String toString() {
        return ("Event(" + stream_id + ", " + window_id + ", " + value + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event) {
            Event other = (Event) obj;

            return value.equals(other.value) && stream_id == other.stream_id && window_id == other.window_id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, stream_id, window_id);
    }
}