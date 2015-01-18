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

/*
 * Below are the libraries required for this project.
 * In this example all of the dependencies are included with the DSE 4.6 distribution.
 * We need to account for that fact in the build.sbt file in order to make sure we don't introduce
 * library collisions upon deployment to the runtime.
 */
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.sql.cassandra.CassandraSQLContext
import org.apache.spark.{SparkContext, SparkConf}

object LoadFromLocalFile {

  /*
   * This is the entry point for the application
   */
  def main(args: Array[String]) {

    /*
     * The first step in this process is to set up the context for configuration for the Spark instance being used.
     * For this example the configuration reflects running DSE/Spark on the local system. In a production system you
     * would want to modify the host and Master to reflect your installation.
     *
     * NOTE: later in this example we use local files as the source of the data. On a remote system you would need to
     *       that your instance of spark had access to those file locations.
     */
    val sparkConf = new SparkConf().set("spark.cassandra.connection.host", "127.0.0.1")
      .setJars(Array("target/scala-2.10/DSE-Spark-HandsOn-assembly-1.0.jar"))
      .setMaster("spark://127.0.0.1:7077")
      .setAppName("DSE Spark HandsOn")
      .set("spark.eventLog.enabled", "false")

    // create a new SparkContext
    val sc = new SparkContext(sparkConf)

    // create a new SparkSQLContext
    val csc = new CassandraSQLContext(sc)

    /*
        In this section we create a native session to Cassandra.
        This is done so that native CQL statements can be executed against the cluster.
     */
    CassandraConnector(sparkConf).withSessionDo { session =>
      /*
       * Make sure that the keyspace we want to use exists and if not create it.
       *
       * Change the topology an replication factor to suit your cluster.
       */
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS spark_cass WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}")


      /*
          Below the data tables are DROPped and re-CREATEd to ensure that we are dealing with new data.
       */

      // Minimum Temperature Table
      session.execute(s"DROP TABLE IF EXISTS spark_cass.station_tmin")
      session.execute(s"CREATE TABLE IF NOT EXISTS spark_cass.station_tmin (st_id text, year int, month int, day int, meas text, value int, yyyymmdd int, PRIMARY KEY(st_id, yyyymmdd))")

      // Maximum Temperature Table
      session.execute(s"DROP TABLE IF EXISTS spark_cass.station_tmax")
      session.execute(s"CREATE TABLE IF NOT EXISTS spark_cass.station_tmax (st_id text, year int, month int, day int, meas text, value int, yyyymmdd int, PRIMARY KEY(st_id, yyyymmdd))")

      // Precipitation Table
      session.execute(s"DROP TABLE IF EXISTS spark_cass.station_prcp")
      session.execute(s"CREATE TABLE IF NOT EXISTS spark_cass.station_prcp (st_id text, year int, month int, day int, meas text, value int, yyyymmdd int, PRIMARY KEY(st_id, yyyymmdd))")

      //Close the native Cassandra session when done with it. Otherwise, we get some nasty messages in the log.
      session.close()
    }

    /*
     * The following three segments of code load local files containing Maximum and Minimum Temperatures as well as Precipitation.
     * The lines are loaded then parsed based on the "," delimiter. Finally, they are loaded via the RDD.saveToCassandra method.
     *
     * The path to these file will need to be altered to reflect path on the target running environment. If you plan on running this
     * example on a remote cluster you will need to transfer the files to those machines and replace the path appropriately.
     */

    // Maximum Temperature
    var lines = sc.textFile(s"file:///Users/carybourgeois/Documents/Training/DSE-Spark-HandsOn/data/sftmax_2003_2013-load.csv")
    var parsedLines = lines.map(line => line.split(","))
    parsedLines.map(p => (p(0), p(1).toInt, p(2).toInt, p(3).toInt, p(4), p(5).toInt, p(6).toInt)).saveToCassandra("spark_cass", "station_tmax", SomeColumns("st_id", "year", "month", "day", "meas", "value", "yyyymmdd"))

    // Minimum Temperature
    lines = sc.textFile(s"file:///Users/carybourgeois/Documents/Training/DSE-Spark-HandsOn/data/sftmin_2003_2013-load.csv")
    parsedLines = lines.map(line => line.split(","))
    parsedLines.map(p => (p(0), p(1).toInt, p(2).toInt, p(3).toInt, p(4), p(5).toInt, p(6).toInt)).saveToCassandra("spark_cass", "station_tmin", SomeColumns("st_id", "year", "month", "day", "meas", "value", "yyyymmdd"))

    // Precipitation
    lines = sc.textFile(s"file:///Users/carybourgeois/Documents/Training/DSE-Spark-HandsOn/data/sfprcp_2003_2013-load.csv")
    parsedLines = lines.map(line => line.split(","))
    parsedLines.map(p => (p(0), p(1).toInt, p(2).toInt, p(3).toInt, p(4), p(5).toInt, p(6).toInt)).saveToCassandra("spark_cass", "station_prcp", SomeColumns("st_id", "year", "month", "day", "meas", "value", "yyyymmdd"))

    /*
     * The following three lines execute the a simple SparkSQL command to verify the number of records that get loaded into
     * the three tables.
     * NOTE: In practice, the output of these three commands is rather had to seperate from the other information that is
     *       generated as part of the INFO/WARN output from the job.
     */
    println("Max Temp Table Record Count: ", csc.sql(s"SELECT COUNT(*) FROM spark_cass.station_tmax").first().foreach(println))
    println("Min Temp Table Record Count: ", csc.sql(s"SELECT COUNT(*) FROM spark_cass.station_tmin").first().foreach(println))
    println("Precipitation Table Record Count: ", csc.sql(s"SELECT COUNT(*) FROM spark_cass.station_prcp").first().foreach(println))


    // Stop the Spark Context. Otherwise, we get some nasty messages in the log.
    sc.stop()

    System.exit(0)
  }

}
