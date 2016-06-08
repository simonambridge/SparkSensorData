	/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.log4j.{Level,Logger}

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming._

import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.cql.CassandraConnector

import java.util.Date



object SparkIngest {

  def createSchema(cc:CassandraConnector, keySpaceName:String, tableName:String) = {
    cc.withSessionDo { session =>
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS ${keySpaceName} WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}")

      session.execute(s"DROP TABLE IF EXISTS ${keySpaceName}.${tableName};")

      session.execute("CREATE TABLE IF NOT EXISTS " +
        s"${keySpaceName}.${tableName} (name text, time timestamp, value decimal, PRIMARY KEY(name, time));")
    }
  }

  def parseMessage(msg:String) : Record = {
    println("parsemessage - data to parse: " + msg)
    val arr = msg.split(",")
    val time = new Date
    return Record(arr(0), time, BigDecimal(arr(1).toFloat))
  }

  case class Record(name:String, time:Date, value:BigDecimal)

  /* This is the entry point for the application */

  def main(args: Array[String]) {

    // Check how many arguments were passed in - none required
    if (args.length >0) {
      System.out.println("No parameters required")
      // System.exit(0);
    }

    /*
     * This next line sets the logger level. If you are having trouble getting this program to work you can change the
     * value from Level.ERROR to LEVEL.WARN or more verbose yet, LEVEL.INFO
     */
    Logger.getRootLogger.setLevel(Level.ERROR)

    /* Set up the context for configuration for the Spark instance being used.
     * Configuration reflects running DSE/Spark on a local system. In a production system you
     * would want to modify the host and Master to reflect your installation.
     */
    val sparkMasterHost = "127.0.0.1"
    val cassandraHost = "127.0.0.1"
    val cassandraKeyspace = "sparksensordata"
    val cassandraTable = "sensordata"

    println("Spark Master Host  = 127.0.0.1")
    println("Cassandra Host     = 127.0.0.1")
    println("Cassandra Keyspace = " + cassandraKeyspace)
    println("Cassandra Table    = " + cassandraTable)

    // Tell Spark the address of one Cassandra node:
    /*
     * The next three .set lines for the SparkConf are important for the Spark Streaming
     * Specifically you have to ensure that you have more than 1 core to run a Spark Streaming App
     * This can either be done at the system level via the config files or as below at the app level
     * The memory number is somewhat arbitrary in this case but 512M is enough here
    */

    println("STEP 1: Defining the Cassandra conf object...")
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", cassandraHost)
      .set("spark.cores.max", "2")
      .set("spark.executor.memory", "512M")
      .set("spark.cleaner.ttl", "3600") // This setting is specific to Spark Streaming. It set a flush time for old items.
//      .setMaster("local[2]")
      .setJars(Array("./target/scala-2.10/sparkportstream_2.10-1.0.jar"))
      .setMaster("spark://127.0.0.1:7077")
//      .setAppName(getClass.getSimpleName)
      .setAppName("DSE Spark Streaming - Ingest Test")
    /*
       * The next two lines that are commented out can be used to trace the execution of the
       * job using the sparkUI. On a local system, where this code would work, the URL for the
       * spark UI would be http://127.0.0.1:7080.
       * Before un-commenting these lines, make sure the spark.eventLog.dir exist and is
       * accessible by the process running spark.
      */
    //.set("spark.eventLog.enabled", "true")
    //.set("spark.eventLog.dir", "/var/log/Datastax/log/spark-events")


    // Connect to the Spark cluster:
    val sc = new SparkContext(conf)
    println("Spark Conf version: " + sc.version)

    // Create a StreamingContext with a SparkConf configuration
    val ssc = new StreamingContext(conf, Seconds(1))

    println("STEP 2: Instantiate the Cassandra connector cc...")
    lazy val cc = CassandraConnector(conf)

    println("STEP 3: Creating SparkSensorData schema...")
    createSchema(cc, cassandraKeyspace, cassandraTable)

    // Create a DStream that will connect to serverIP:serverPort
    val lines = ssc.socketTextStream("localhost", 9999)

    println("STEP 4: Parsing incoming data...<ID>,<timestamp>,<value>")
    val Words = lines.flatMap(_.split(","))   // will give two words per row received - ID and value
    val rowToInsert = lines.map(parseMessage) // will give three words per row received - ID, timestamp and value


    println("STEP 5: Save to Cassandra..." + Words)
    rowToInsert.saveToCassandra(cassandraKeyspace, cassandraTable)

    ssc.start()
    ssc.awaitTermination()

  }

}


