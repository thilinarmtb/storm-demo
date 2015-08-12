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

public class ProbabilisticallyAckingBolt implements IRichBolt {
    private OutputCollector _collector;
    private String _name;
    private double _probabilityThreshold;
    private double eps = 0.000001;
    private static transient Logger log = Logger.getLogger(ProbabilisticallyAckingBolt.class);

    public ProbabilisticallyAckingBolt(String name, double probabilityThreshold) {
        _name = name;
        _probabilityThreshold = probabilityThreshold;
    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    public void execute(Tuple input) {
        String word = input.getString(0);
        _collector.emit(input, new Values("ProbabilisticallyAckingBolt " + word + " !!"));
        log.info(_name + " Received word: " + word);

        if (Math.random() > _probabilityThreshold) {
            _collector.ack(input);
            log.info(_name + " Acked word: " + word);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("wordStream"));
    }

    public void cleanup() {
    }

    public Map getComponentConfiguration() {
        return null;
    }
}

