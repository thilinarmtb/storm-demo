/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package storm.demo.bolt;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

// Counts the words received by the Bolt. We keep an in memory hash map to
// store the counts. Please have a look at IRichBolt interface to know
// more about the overridden methods.
// IRichBolt: https://storm.apache.org/apidocs/backtype/storm/topology/IRichBolt.html
public class WordCount implements IRichBolt {
    private OutputCollector _collector;
    // This hash map holds the count keyed by the word.
    private Map<String, Integer> counts = new HashMap<String, Integer>();
    private static transient Logger log = Logger.getLogger(WordCount.class);

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    // This method processes the tuples received.
    public void execute(Tuple input) {
        String word = input.getString(0);
        Integer count = counts.get(word);
        if (count == null) {
            // We haven't seen the word before
            count = 0;
        }
        // Increment the count and put it back
        count ++;
        counts.put(word, count);
        log.info(" Received word: " + word + ", Count: " + count);
        // Acknowledge that the tuple was processed successfully.
        _collector.ack(input);
    }

    // Declare (a) name(s) for the field(s) emitted by the Bolt.
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("wordStream"));
    }

    public void cleanup() {}

    public Map getComponentConfiguration() {
        return null;
    }
}
