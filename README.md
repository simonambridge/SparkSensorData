#DataStax Enterprise - Apache Cassandra with Apache Spark - Sensor Data Ingestion

Demonstrate high speed ingestion of sensor data
-----------------------------------------------

This demo demonstrates how data can be streamed to a Spark receiver listening to a network port.
The demo consists of three parts:
- netCat       - a data generator written in Java
- SparkIngest  - a Spark streaming job written in Scala
- casChart     - simple way to visualise simple data using java

##Pre-Requisites
To setup your environment, you'll need the following resources:

###Install sbt
sbt is the Scala Build Tool

```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
sudo apt-get update
sudo apt-get install sbt
```

###Install code dependencies
```
http://xeiam.com/xchart-example-code/
http://slf4j.org/dist/slf4j-1.7.10.tar.gz
http://downloads.datastax.com/java-driver/cassandra-java-driver-2.0.2.tar.gz
cassandra-driver-core-2.1.0.jar
netty-3.9.0-Final.jar
guava-16.0.1.jar
metrics-core-3.0.2.jar
```

>See ```build.sbt``` for more details.

###Useful Linux tool - ```locate```
You can use this to easily locate files. I like it.
```
apt-get install locate
```
Then scan the filesystems for the first time to set up ```locate```.
```
updatedb
```
Thereafter find any file using e.g.:
```
locate <file to find>
```

##Exercise 1 - send data by hand

Open three terminal windows. First you'll need to clone the repo in one of the windows.

```
$ git clone https://github.com/simonambridge/SparkSensorData
```
Now navigate to the repo directory in each of the windows:
```
$ cd ~/SparkSensorData
$ pwd
~/SparkSensorData
```

In one window check that you can build the package from the command line using **sbt**:
```
$ sbt package
[info] Loading project definition from /home/dse/Simon-demo/project
[info] Set current project to SparkPortStream (in build file:/home/dse/Simon-demo/)
[info] Compiling 1 Scala source to /home/dse/Simon-demo/target/scala-2.10/classes...
[info] Packaging /home/dse/Simon-demo/target/scala-2.10/sparkportstream_2.10-1.0.jar ...
[info] Done packaging.
[success] Total time: 9 s, completed Mar 21, 2015 2:28:15 PM
```

It's important to do the steps in the right order.
First we will start the Linux netcat utility - netcat allows us to send data to a port. We will use port 9999 for this. 

In one of the terminal windows type:
```
nc -lk localhost 9999
````
Netcat is now sitting there 'connected' to port 9999 on the local machine.

In another terminal window start the Spark job. This will receive any data that we will send using netcat.

The Spark job takes three parameters 
- the hostname or IP of the  host running the Cassandra database that we will store data in
- the hostname or IP of the Spark Master node
- the port to use to stream/receive the data

```
dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar 127.0.0.1 127.0.0.1 9999
```
You get some output confirming what's happening when the Spark job starts:
```
Spark Master Host   = 127.0.0.1
Cassandra Host      = 127.0.0.1
Streaming data port = 9999

Cassandra Keyspace = sparksensordata
Cassandra Table    = sensordata

STEP 1: Defining the Cassandra conf object...
STEP 2: Connect to the Spark cluster...
Spark Conf version: 1.4.2
STEP 3: Create a StreamingContext...
STEP 4: Instantiate the Cassandra connector cc...
STEP 5: Creating SparkSensorData schema...
STEP 6: Parsing incoming data...<ID>,<value> and save to Cassandra

```
if you dont provide three parameters you'll get an error before exiting:
```
Error - one or more missing parameters
Usage is:
dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar <cassandraHost> <sparkMasterHost> <data port>
```

> **NB** You can ignore any connection refused errors like that shown below - this happens when the streaming job isn't receiving any data.
```
ERROR 2016-06-10 14:49:58,195 org.apache.spark.streaming.scheduler.ReceiverTracker: Deregistered receiver for stream 0: Restarting receiver with delay 2000ms: Error connecting to localhost:9999 - java.net.ConnectException: Connection refused
```


In a third window start cqlsh:
```
cqlsh `hostname`
```
And see the records in the sparsensordata.sensordata table:
```
> select * from sparsensordata.sensordata;
```
We will re-run this query as required to demonstrate the data arriving from the Spark job.

Now go back to the second window (to run netcat) and type in some comma separated pairs of data e.g.
```
nc -lk localhost 9999
1,2
1,3
1,4
1,999
```
**NB** if you do not use commas you will generate an ```java.lang.ArrayIndexOutOfBoundsException``` error and the Spark job will fail.

In the cqlsh window run the query again and you will see the data pars that you typed in arriving in the sensordata table:
```
> select * from sparksensordata.sensordata ;

 name | time                     | value
------+--------------------------+-------
    1 | 2016-06-10 13:08:52+0000 |   2.0
    1 | 2016-06-10 13:08:52+0000 |   3.0
    1 | 2016-06-10 13:08:52+0000 |   4.0
    1 | 2016-06-10 13:08:57+0000 | 999.0

(4 rows)
```

You can try pushing the contents of the csv file (150 records) to the port before starting the Spark job:
```
cat SensorData2.csv | nc -lk localhost 9999
```
Then run the Spark job and see how many records there are in Cassandra - about 35 to 40?


##Exercise 2 - use a data generator

For this exercise we'll use the Java data generator netCat.java:

Navigate to the source directory and compile the netCat.java source file:
```
cd ./src/main/java
javac netCat.java
```
Java netCat takes four parameters:
- data type - generate a linear or non-linear dataset
- sample rate in ms
- number of samples to send
- streaming port to send the data to


In this example Java netCat will send 1000 non-linear samples @ 50 samples per second (1 per 20ms) to port 8080
```
$ java netCat n 100 1000 8080
*****************************************
Data sample type: Non-linear
Data sample rate: 20ms
Data sample count: 1000
*****************************************
Waiting for listener........
```
And we would see output like this:
```
p100,0.9714263278601847
p100,0.3364444649925371
p100,0.6484636848309043
p100,1.5735030153001182
p100,1.599807138977728
p100,1.5585592457603932
p100,3.9639281226659615
p100,5.161374045313633
```

OK, lets test it.

First we can test Java netCat to show how it pushes data to a network port. 

In a terminal window start a Java netCat run:
```
java netCat n 20 100 9999
*****************************************
Data sample type: Non-linear
Data sample rate: 20ms
Data sample count: 100
*****************************************
Waiting for listener........
```
>**NB** netCat sits and waits for a listener on the port (to receive the streamed data) before it sends anything.

In another terminal window use nc to listen for output: 
```
$ nc localhost 9999
```

Data now appears in both windows - in both the window sending data to the port, and in the window where we told nc to listen to the port..
```
p100,0.06769868828261028
p100,1.1106579192248016
p100,0.933662716664309
p100,3.489371007397343
p100,3.7282250967333344
p100,5.281761403281172
p100,4.822815663629292
p100,1.305075478974862
p100,1.5396150741347898
```

Now lets use it with Spark streaming...

Start netCat as shown above to send some data - 100 non-linear (pseudo random) samples @ two samples per second:
```
java netCat n 500 100 9999

*****************************************
Data sample type: Non-linear
Data sample rate: 500ms
Data sample count: 100
*****************************************
Waiting for listener........
```

Nothing happens until the receiver starts taking data from the port.

Start the Spark streaming job to stream from the port:
```
dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar 127.0.0.1 127.0.0.1 9999
```
The job output tells you what its doing:
```
Spark Master Host   = 127.0.0.1
Cassandra Host      = 127.0.0.1
Streaming data port = 9999

Cassandra Keyspace = sparksensordata
Cassandra Table    = sensordata

STEP 1: Defining the Cassandra conf object...
STEP 2: Connect to the Spark cluster...
Spark Conf version: 1.4.2
STEP 3: Create a StreamingContext...
STEP 4: Instantiate the Cassandra connector cc...
STEP 5: Creating SparkSensorData schema...
STEP 6: Parsing incoming data...<ID>,<value> and save to Cassandra
```

In the Java netCat window you should see records being written:
```
p100,1.1106579192248016
p100,0.933662716664309
p100,3.489371007397343
p100,3.7282250967333344
p100,5.281761403281172
```

In cqlsh check that records are being written to the table:
```
cqlsh:demo> select * from sensordata;

 name | time                     | value
------+--------------------------+--------------------
 p100 | 2015-03-23 11:37:40+0000 | 0.9714263081550598
 p100 | 2015-03-23 11:37:41+0000 | 0.3364444673061371
 p100 | 2015-03-23 11:37:41+0000 | 0.6484636664390564
 p100 | 2015-03-23 11:37:42+0000 | 1.5998071432113647
 p100 | 2015-03-23 11:37:42+0000 |  1.573503017425537
 p100 | 2015-03-23 11:37:43+0000 | 1.5585592985153198
 p100 | 2015-03-23 11:37:43+0000 |   3.96392822265625
 p100 | 2015-03-23 11:37:44+0000 |  5.161374092102051
 p100 | 2015-03-23 11:37:44+0000 | 3.0495009422302246
 p100 | 2015-03-23 11:37:45+0000 | 0.3547881245613098
```

You can re-run the netCat job again while the Spark job is still running to inject more data into Cassandra.


##Visualising The Data In Cassandra

The idea here is just to make a simple way of viewing the data generated and streamed into Cassandra. You could use something like this to build a dashboard (but it would need to be a lot prettier than this....).

This example uses the Datastax Cassandra driver and xChart graph libraries. You will also need slf4j, guava and netty.
```
$ cd ./src/main/java
```
```
$ javac -cp .:<path to Cassandra driver jars>/cassandra-java-driver-2.0.2/cassandra-driver-core-2.0.2.jar:<path to xchart jars>/xchart-2.4.3/xchart-2.4.3.jar casChart.java
```
```
$ java -cp .:<path to slf4j jars>/slf4j/slf4j-1.7.10/slf4j-api-1.7.10.jar:<path to Netty jars>/netty-3.9.0.Final.jar:<Path to guava jars>/guava-16.0.1.jar:<path to Datastax metrics>/Datastax/metrics-core-3.0.2.jar:<path to Cassandra driver>/cassandra-java-driver-2.0.2/cassandra-driver-core-2.0.2.jar:<path to xchart jars>/xchart-2.4.3/xchart-2.4.3.jar casChart

SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Mon Mar 23 11:37:40 GMT 2015, 0.9714263081550598
Mon Mar 23 11:37:41 GMT 2015, 0.3364444673061371
Mon Mar 23 11:37:41 GMT 2015, 0.6484636664390564
Mon Mar 23 11:37:42 GMT 2015, 1.5998071432113647
Mon Mar 23 11:37:42 GMT 2015, 1.573503017425537
Mon Mar 23 11:37:43 GMT 2015, 1.5585592985153198
```

Graphed data will appear in a window.






