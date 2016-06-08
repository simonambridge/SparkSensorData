#DataStax Enterprise - Apache Cassandra with Apache Spark - Sensor Data Ingestion

Demonstrate high speed ingestion of sensor data
-----------------------------------------------

##Pre-Requisites
To setup your environment, you'll need the following resources:
```
http://xeiam.com/xchart-example-code/
http://slf4j.org/dist/slf4j-1.7.10.tar.gz
http://downloads.datastax.com/java-driver/cassandra-java-driver-2.0.2.tar.gz
cassandra-driver-core-2.1.0.jar
netty-3.9.0-Final.jar
guava-16.0.1.jar
metrics-core-3.0.2.jar
```

##Exercise 1

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

Test that you can build the package from the command line using **sbt**:
```
$ sbt package
[info] Loading project definition from /home/dse/Simon-demo/project
[info] Set current project to SparkPortStream (in build file:/home/dse/Simon-demo/)
[info] Compiling 1 Scala source to /home/dse/Simon-demo/target/scala-2.10/classes...
[info] Packaging /home/dse/Simon-demo/target/scala-2.10/sparkportstream_2.10-1.0.jar ...
[info] Done packaging.
[success] Total time: 9 s, completed Mar 21, 2015 2:28:15 PM
```

Start the Spark job - *ignore connection refused errors as the port isnt listening yet.
```
$ dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar -Dspark.cassandra.connection.host=127.0.0.1
```
In another window:

$ head SensorData2.csv
p100,1
p100,2
p100,3
p100,4
p100,5
p100,6
p100,7

1. Test 1 - Slow-speed test....

Push records in at 1 per second (or run push.sh):

$ for i in `cat SensorData2.csv` 
do
echo $i | nc -l 9999
sleep 1
done

In another window:

cqlsh> use demo;

cqlsh:demo> select * from sensor_data;

 name | time                     | value
------+--------------------------+-------
 p100 | 2015-03-21 17:04:29+0000 |   1.0
 p100 | 2015-03-21 17:04:29+0000 |   2.0
 p100 | 2015-03-21 17:04:29+0000 |   3.0
 p100 | 2015-03-21 17:04:29+0000 |   4.0
 p100 | 2015-03-21 17:04:29+0000 |   5.0
 p100 | 2015-03-21 17:04:29+0000 |   6.0
 p100 | 2015-03-21 17:04:29+0000 |   7.0
 p100 | 2015-03-21 17:04:29+0000 |   8.0
 p100 | 2015-03-21 17:04:29+0000 |   9.0
 p100 | 2015-03-21 17:04:29+0000 |  10.0
 p100 | 2015-03-21 17:04:29+0000 |  11.0
 p100 | 2015-03-21 17:04:29+0000 |  12.0
 p100 | 2015-03-21 17:04:29+0000 |  13.0
 p100 | 2015-03-21 17:04:29+0000 |  14.0
 p100 | 2015-03-21 17:04:29+0000 |  15.0
 p100 | 2015-03-21 17:04:29+0000 |  16.0
 p100 | 2015-03-21 17:04:29+0000 |  17.0



2. Test 2 - High-speed test....

$ cat SensorData2.csv | nc -l 9999

NB 10% records dropped at 150/sec


Phase 2.
========
Java data generator netCat.java:

$ cd /home/dse/Simon-demo/src/main/java
$ javac netCat.java
$ java netCat <Data type: l|nl> <Sample rate in ms> <Number of Samples>
e.g.
$ java netCat l 500 1000 (= 1000 linear samples @ 2 per second)
p100,1
p100,2
p100,3
p100,4
p100,5
p100,6

or
$ java netCat n 20 1000 (= 1000 non-linear samples @ 50 per second) 
p100,0.9714263278601847
p100,0.3364444649925371
p100,0.6484636848309043
p100,1.5735030153001182
p100,1.599807138977728
p100,1.5585592457603932
p100,3.9639281226659615
p100,5.161374045313633

(netCat will sit there until something else listens on the port)

Then, to test it open another terminal: 
$ nc localhost 9999 - data will appear

Data appears in both windows.

To use it:
Start netCat as shown above
Start the Spark streaming job:
$ cd /home/dse/Simon-demo
$ dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar -Dspark.cassandra.connection.host=127.0.0.1

Check records appearing in the table.
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






