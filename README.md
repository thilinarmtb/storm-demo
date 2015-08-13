# Storm Demonstrations

## Introduction

This repository contains four storm topologies which are used to demonstrate
two things: at least once processing semantics and stateless processing of
Storm.

I hope to demonstrate these features by running these topologies in a locally
created multi-node cluster. While the topology is running, we will selectively
kill some of the worker nodes and conclude that storm can provide at least once
semantics and stateless processing by looking at the logs printed in the worker
nodes.

### Demo scenarios

#### Demo1: Message replay

We play `n` number of messages from a spout to a bolt and ack all except one
of the messages in the bolt. 

**Observation**:
We can see the message that didn’t ack gets replayed forever.

**Conclusion**:
Storm guarantees at least once processing by replaying messages till they are
successfully processed (If we use an anchored topology).

#### Demo2: Message replay to all the bolts

We play `n` number of messages (not necessarily distinct) from a spout to two
bolts each directly connected to the spout. One bolt (say, `Bolt1`) acks all
the messages it receives and the other (`Bolt2`) acks all except one of the
messages.

**Observation**:
We can see that even though `Bolt1` acked all the messages, when the message not
acked by `Bolt2` is replayed, `Bolt1` too will receive the messages replayed.

**Conclusion**:
In case of an ack failure, failed message is replayed to all the bolts in the
topology (in an anchored topology).

#### Demo3: Count the number of Messages received

We play `n` number of messages (not necessarily distinct) from a single spout to
two bolts(`Bolt1` and `Bolt2`)  directly connected to the spout. One bolt acks
messages randomly (i.e., it sometimes acks the messages and sometimes
doesn’t). We count the messages received by each bolt.

**Observation**:
We can see that even though `Bolt1` acked all the messages, when the message not
acked by `Bolt2` is replayed, `Bolt1` too will receive the messages replayed.

**Conclusion**:
In case of an ack failure, failed messages are replayed and this leads to a wrong
state (in these case count) in the bolts.

#### Demo4: Crash of a worker

We play `n` number of messages (not necessarily distinct) from a single spout to
a bolt directly connected to the spout. Bolt is counting the words, i.e., How many
times a given word is received by the bolt. Then we crash one worker (either Bolt
or Spout) and see the effect of the final outcome.

**Observation**:
When we crash the Spout, we can see that the Bolt over counts. When we crash the bolt
we can see that the Bolt under counts.

**Conclusion**:
When we crash the Spout and restart, it starts to replay all the messages from the
beginning again. So we over count. When we crash the Bolt and restart, we loose
the current count in the Bolt and starts to count from zero. So we under count.

## Setting up the environment

The most difficult thing we have to accomplish in order to successfully run the
topologies is locally setting up a multi-node cluster. I referred the excellent
article written by Michael G. Noll to do this. You can find the article in [1].
Please follow the above-mentioned article carefully. We need to pay special
attention for creating the multi-node cluster locally.

### Local multi-node cluster

The article in [1] describes in detail a general approach to configure an actual
multi-node cluster using multiple physical nodes. For most of us, access to that
kind of resources is not possible. So we are going to mimic this environment locally.

The only thing you have to do differently from the tutorial is to create multiple
storm directories locally and update their `conf/storm.yaml` so that the supervisors
of each local copy run on different ports. You can download storm releases from here.
I used 0.9.2-incubating version. You need to download a release, extract it and
rename the extracted directory depending on the tasks you need that node to perform.
Below is the folder structure of my cluster.
```
├── cluster
    ├── nimbus
    ├── worker1
    ├── worker2
    ├── worker3
    ├── zookeeper-3.4.6
```

I have additionally added a zookeeper node as well. I am using zookeeper
version 3.4.6. 

Only difference (except the names of the directories) between `nimbus`,
`worker1`,`... worker4` is their configuration file `storm.yaml` found
under `conf/` in each directory. I have listed below the configuration
files I used for each of the nodes.

[nimbus](https://gist.github.com/thilinarmtb/85980741bcd90c483827)

[worker1](https://gist.github.com/thilinarmtb/2271b0eb9db5610dd636)

[worker2](https://gist.github.com/thilinarmtb/015de16702e372d810f5)

[worker3](https://gist.github.com/thilinarmtb/3264353b84cb2b66b9e7)

## Building the Demonstrations

Create an Uberjar of this repository by doing:
```
mvn clean package -Pcluster
```

This will create a jar called `demo-1.0-SNAPSHOT-jar-with-dependencies.jar` under
`target/` folder.

## Running the Demonstrations

Move to the zookeeper directory and start the zookeeper using the following
command.
```
bin/zkServer.sh start
```

Move to the nimbus node and start the nimbus by issuing the following command.
```
bin/storm nimbus
```

Next move to the worker nodes and start the supervisors using the following
command (We have three worker nodes).
```
bin/storm supervisor
```

Now we need to look at the logs of each worker node. Logs are stored under `logs/`
directory inside each worker node. Suppose we want to see how the logs update
in the `worker1` node. Then we can use the following command to see it's log:
```
tail -f logs/worker-6700.log
```

The number `6700` comes from the supervisor port number assigned to the worker
node. Since we have only assigned one port number to each worker node, we can
see worker node's full log this way.

Next we need to upload the topology to storm cluster. To do that move to any
node (nimbus or a worker node) and type the following command:
```
bin/storm jar <path_to_the_jar_we_created>/demo-1.0-SNAPSHOT-jar-with-dependencies.jar storm.demo.Demo<n> demo<n>
```

here `<n>` is the demo number (1, 2, 3 or 4).

**Note**: For the 4th demo, you may need to kill the worker nodes. To do that
go to the terminal session where you started it's supervisor and use Ctrl-C to
kill the process.

## References

[1] Running a Multi-Node Storm Cluster: http://www.michael-noll.com/tutorials/running-multi-node-storm-cluster/
