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

// `RepeatWords` Spout repeats the set of words {apple, ball, cat, dog} a pre-specified number
// of times. This does the same thing as the `FixedWords` spout but with repeats.
// Please refer the apidocs for IComponent and ISpout to know more about
// the overridden methods.
// ISpout: https://storm.apache.org/apidocs/backtype/storm/spout/ISpout.html
// IComponent: https://storm.apache.org/apidocs/backtype/storm/topology/IComponent.html
public class RepeatWords extends BaseRichSpout {
    SpoutOutputCollector _collector;

    // Specify the number of times the words are repeated.
    private int _count;
    // Specify the delay between two emitted words.
    private long _delay;
    // Counter to keep track of the tuples emitted.
    private int current = 0;
    // Set of words, one of these will be emitted when `nextTuple` is called.
    private String[] words = new String[]{"apple", "ball", "cat", "dog"};

    private static transient Logger log = Logger.getLogger(RepeatWords.class);

    public RepeatWords(int count, long delay) {
        _count = count;
        _delay = delay;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
    }

    // This function emits the tuple.
    @Override
    public void nextTuple() {
        if (current < _count*words.length) {
            String word = words[current % 4];
            // We emit the word with the id `current`.
            _collector.emit(new Values(word), current);
            log.info("Emitted word: " + word + " Current: " + current);
            current++;
            Utils.sleep(_delay);
        }
    }

    // This method is called when a message is successfully process. If we were using something like a
    // queue of messages, we might remove it from the queue to avoid repeating the same thing again and
    // again. But in our case, we don't want to do anything.
    @Override
    public void ack(Object msgId) {
    }

    // This function will be called when a tuple fails and `msgId` contains
    // the fail message id.
    @Override
    public void fail(Object msgId) {
        String word = words[((Integer) msgId)%4];
        log.info("Replaying word: " + word);
        _collector.emit(new Values(word), msgId);
    }

    // Declare (a) name(s) for the field(s) emitted by the Spout.
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("words"));
    }
}
