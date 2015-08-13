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

package storm.demo.spout;

import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

// `FixedWords` Spout emits the set of words {apple, ball, cat, dog} once.
// Please refer the apidocs for IComponent and ISpout to know more about
// the overridden methods.
// ISpout: https://storm.apache.org/apidocs/backtype/storm/spout/ISpout.html
// IComponent: https://storm.apache.org/apidocs/backtype/storm/topology/IComponent.html
public class FixedWords extends BaseRichSpout {
    private SpoutOutputCollector _collector;

    private static transient Logger log = Logger.getLogger(FixedWords.class);

    // Counter to keep track of the tuples emitted.
    private int count = 0;
    // Set of words, one of these will be emitted when `nextTuple` is called.
    private String[] words = new String[]{"apple", "ball", "cat", "dog"};

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
    }

    // This function emits the tuple.
    @Override
    public void nextTuple() {
        Utils.sleep(10000);
        if (count < words.length) {
            String word = words[count];
            // We emit the word with the id `current`.
            _collector.emit(new Values(word), count);
            log.info("Emitted word: " + word);
            count++;
        }
    }

    // This method is called when a message is successfully process. If we were using something like a
    // queue of messages, we might remove it from the queue to avoid repeating the same thing again and
    // again. But in our case, we don't want to do anything.
    @Override
    public void ack(Object id) {
    }

    // This function will be called when a tuple fails and `msgId` contains
    // the fail message id.
    @Override
    public void fail(Object id) {
        String word = words[(Integer) id];
        log.info("Replaying word: " + word);
        _collector.emit(new Values(word), id);
    }

    // Declare (a) name(s) for the field(s) emitted by the Spout.
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }

}
