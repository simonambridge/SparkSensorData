#DataStax Enterprise - Apache Cassandra with Apache Spark - Sensor Data Ingestion

Demonstrate high speed ingestion of sensor data
-----------------------------------------------

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

###Useful tool - locate
You can use this to easily locate files. I like it.
```
apt-get install locate
```
Then scan the filesystems for the first time
```
updatedb
```
Thereafter find any file using e.g.:
```
locate <file to find>
```

##Exercise 1 - send data by hand

Open three terminal windows. First you'll need to clone the repo

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
First we will start netcat - we will choose port 9999 for this. In one of the terminal windows type:
```
nc -lk localhost 9999
````

In another terminal windows start the Spark job
It takes three parameters 
- the hostname or IP of the Cassandra host
- the hostname or IP of the Spark Master node
- the port to use to stream the data

```
dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar 127.0.0.1 127.0.0.1 9999
```
if you dont provide three parameters you'll get an error before exiting:
```
Error - one or more missing parameters
Usage is:
dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar <cassandraHost> <sparkMasterHost> <data port>
```

**NB** You can ignore any connection refused errors like that shown below - this happens when the streaming job isn't receiving any data.
```
ERROR 2016-06-10 14:49:58,195 org.apache.spark.streaming.scheduler.ReceiverTracker: Deregistered receiver for stream 0: Restarting receiver with delay 2000ms: Error connecting to localhost:9999 - java.net.ConnectException: Connection refused
```


In the third window start cqlsh and see the records in the sparsensordata.sensordata table:
```
cqlsh `hostname`
```

Now go back to the second window (netcat) and type in some comma separated pairs of data e.g.
```
nc -lk localhost 9999
1,2
1,3
1,4
1,999
```
**NB** if you do not use commas you will generate an ```java.lang.ArrayIndexOutOfBoundsException``` error and the Spark job will fail.

In the cqlsh window you should see your data arriving in the sensordata table:
```
cqlsh:sparksensordata> select * from sparksensordata.sensordata ;

 name | time                     | value
------+--------------------------+-------
    1 | 2016-06-10 13:08:52+0000 |   2.0
    1 | 2016-06-10 13:08:52+0000 |   3.0
    1 | 2016-06-10 13:08:52+0000 |   4.0
    1 | 2016-06-10 13:08:57+0000 | 999.0

(4 rows)
```

You can also push the contents of the csv file (150 records) to the port before starting the Spark job:
```
cat SensorData2.csv | nc -lk localhost 9999
```
Then run the Spark job and see how many records there are in Cassandra - about 35 to 40?


##Exercise 2 - use a data generator

For this exercise we'll use a Java data generator netCat.java:

Navigate to the source directory and compile the netCat.java source file:
```
cd ./src/main/java
javac netCat.java
```
netCat takes four parameters:
- data type - linear or non-linear
- sample rate in ms
- number of samples
- streaming port

For example neCat will send 1000 linear samples @ 2 per second (1 per 500ms) to port 9999
```
$ java netCat l 500 1000 9999
p100,1
p100,2
p100,3
p100,4
p100,5
p100,6
```
In this example netCat will send 1000 non-linear samples @ 50 per second (1 per 20ms) to port 8080
```
$ java netCat n 20 1000 8080
p100,0.9714263278601847
p100,0.3364444649925371
p100,0.6484636848309043
p100,1.5735030153001182
p100,1.599807138977728
p100,1.5585592457603932
p100,3.9639281226659615
p100,5.161374045313633
```


First we can test netCat. In one terminal window type one of the examples above:
```
java netCat n 20 1000 9999
*****************************************
Data sample type: Non-linear
Data sample rate: 20ms
Data sample count: 1000
*****************************************
Waiting for listener........
```
netCat now waits for a listener on the port to receive the streamed data.

In another terminal window use nc to listen for output: 
```
$ nc localhost 9999
```

Data appears in both windows.
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

Now lets use it with Spark streaming

Start netCat as shown above:
```
java netCat n 20 1000 9999
```

Start the Spark streaming job:
```
dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar 127.0.0.1 127.0.0.1 9999
```
Check records appearing in the table:
```
cqlsh:demo> select * from sensor_data;

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

You can run the netCat job again while the Spark job is still running to inject more data into Cassandra.


Graphing
========
This utility uses the Datastax Cassandra driver and xChart graph libraries.

$ cd /home/dse/Simon-demo/src/main/java

$ javac -cp .:/home/dse/Datastax/cassandra-java-driver-2.0.2/cassandra-driver-core-2.0.2.jar:/home/dse/xchart-2.4.3/xchart-2.4.3.jar casChart.java

$ java -cp .:/home/dse/slf4j/slf4j-1.7.10/slf4j-api-1.7.10.jar:/Software/Datastax/netty-3.9.0.Final.jar:/Software/Datastax/guava-16.0.1.jar:/Softwe/Datastax/metrics-core-3.0.2.jar:/home/dse/Datastax/cassandra-java-driver-2.0.2/cassandra-driver-core-2.0.2.jar:/home/dse/xchart-2.4.3/xchart-2.4.3.jar casChart

SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Mon Mar 23 11:37:40 GMT 2015, 0.9714263081550598
Mon Mar 23 11:37:41 GMT 2015, 0.3364444673061371
Mon Mar 23 11:37:41 GMT 2015, 0.6484636664390564
Mon Mar 23 11:37:42 GMT 2015, 1.5998071432113647
Mon Mar 23 11:37:42 GMT 2015, 1.573503017425537
Mon Mar 23 11:37:43 GMT 2015, 1.5585592985153198

Graphed data will appear in a window.






