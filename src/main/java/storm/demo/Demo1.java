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

package storm.demo;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import storm.demo.bolt.SingleNonAckingBolt;
import storm.demo.spout.FixedWords;

public class Demo1 {
    public static void main(String[] args) throws Exception {
        // We create a topology with a single Spout and a Bolt.
        // `FixedWords` spout emits four words. `SingleNonAckingBolt` acks three
        // of the words leaving out one.
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("words", new FixedWords());
        builder.setBolt("Bolt", new SingleNonAckingBolt("Bolt"), 1).shuffleGrouping("words");

        // `conf` stores the configuration information that needs to be supplied with
        // the strom topology.
        Config conf = new Config();
        conf.setDebug(false);

        if (args != null && args.length > 0) {
            // Submit the topology to the cluster.
            conf.setNumWorkers(2);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        } else {
            // Run locally if no input arguments are present.
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(10000);
            cluster.killTopology("test");
            cluster.shutdown();
        }
    }
}
