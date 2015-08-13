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

import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

// Acks all the words received by the Bolt. Please have a look at IRichBolt interface to know more
// about the overridden methods.
// IRichBolt: https://storm.apache.org/apidocs/backtype/storm/topology/IRichBolt.html
public class AllAckingBolt implements IRichBolt {
    private OutputCollector _collector;
    private String _name;
    private static transient Logger log = Logger.getLogger(AllAckingBolt.class);

    public AllAckingBolt(String name) {
        _name = name;
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    // This method processes the tuples received.
    public void execute(Tuple input) {
        String word = input.getString(0);
        _collector.emit(input, new Values(word));
        log.info(_name + " Received word: " + word);
        _collector.ack(input);
    }

    // Declare (a) name(s) for the field(s) emitted by the Bolt.
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("wordStream"));
    }

    public void cleanup() {
    }

    public Map getComponentConfiguration() {
        return null;
    }
}

