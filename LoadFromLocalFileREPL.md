#Overview

In this step we will perform the following steps:

1. Locate and review the source data for the new tables
2. Prepare a Cassandra table in the spark_cass keyspace for the new data
3. Create a spark RDD from the data in the file and load it into the Cassandra table
4. Query the table to ensure that data was correctly loaded

#Requirements

Local copy of DSE 4.6 installed (This example is based on a tarball install on Mac OS X). Make sure you can get to and interact with the Spark REPL included with DSE.

##1. Locate and review the source data for the new tables

In order to work with this exercise and the remaining exercises in this example you should clone the git repository to the machine where DSE is located.

From the root direct of the downloaded project, move to the data sub directory and ensure that you have the sfpnax_2003_2013-load.csv file. The contents of this file is the daily maximum temperature for the ????

Below is an example of the contents of the file.

        USC00047767,2003,01,01,TMAX,144,20030101
        USC00047767,2003,01,02,TMAX,144,20030102
        USC00047767,2003,01,03,TMAX,167,20030103
The fields are:

  * Station ID -- Text
  * Year -- int
  * Month -- int
  * Day -- int
  * Measurement -- text
  * Value -- int
  * YYYYMMDD -- int

##2. Prepare a Cassandra table in the spark_cass keyspace for the new data

First you need to decide how you want to model/specify the Cassandra table. below is one possible way. Keep in mind that the use case for SparkSQL is the ability to overcome some of the limitations of the data model imposed by Cassandra. So feel free to adjust this table structure as it should have no impact on out se case.

CREATE TABLE IF NOT EXISTS spark_cass.station_max (st_id int, year int, month int, day int, meas text, value int, YYYYMMDD int, PRIMARY KEY(st_id, YYYYMMDD))

With the structure decided we need to create this table so that we have a place to put the data. Us the following step from the Spark REPL.

        import com.datastax.spark.connector.cql.CassandraConnector

        val connector = CassandraConnector(csc.conf)
        val session = connector.openSession()

        session.execute(s"DROP TABLE IF EXISTS spark_cass.station_tmax")
        session.execute(s"CREATE TABLE IF NOT EXISTS spark_cass.station_tmax (st_id text, year int, month int, day int, meas text, value int, YYYYMMDD int, PRIMARY KEY(st_id, YYYYMMDD))")

        session.close()

You can validate that the table was created successfully by using Spark SQL to create a Schema RDD and check that the field types are correct.

  * Execute a select from the table that you have just created. It will not contain any rows but it will create a schemaRDD that has the structure of the table you just created.

        val emptyRDD = csc.sql(s"SELECT * FROM spark_cass.station_tmax")

  * You can count the number of records. It should return 0

        emptyRDD.count()

  * You can get the structure of the schemaRDD using the "printSchema" method. The results should look like the table below.

        emptyRDD.printSchema()

        root
         |-- st_id: string (nullable = true)
         |-- yyyymmdd: integer (nullable = true)
         |-- day: integer (nullable = true)
         |-- meas: string (nullable = true)
         |-- month: integer (nullable = true)
         |-- value: integer (nullable = true)
         |-- year: integer (nullable = true)

##3. Create a spark RDD from the data in the file and load it into the Cassandra table

In this step you will read a file from the local disk and then split the records into fields using the delimiter.

  * In this first line of code you will specify the location of the file. In this case we have a fully qualified local file name. Note that you can also use other qualifiers such as cfs or hdfs.

        val lines = sc.textFile("file:///Users/carybourgeois/Documents/Training/DSE-Spark-HandsOn/data/sftmax_2003_2013-load.csv")

  * Count the number of lines in the file. The result should be 3606.

        lines.count()

  * The next step is to parse the lines into the individual fields. In this example the fields are delimited with a ",".

        val parsedLines = lines.map(line => line.split(","))

  * Finally, load the fields into the Cassandra table you prepared. Notice that we need to map/specify the fields and types that will be pushed into Cassandra (default type is text)

        parsedLines.map(p => (p(0), p(1).toInt, p(2).toInt, p(3).toInt, p(4), p(5).toInt, p(6).toInt)).saveToCassandra("spark_cass", "station_tmax", SomeColumns("st_id", "year", "month", "day", "meas", "value", "yyyymmdd"))

##4. Query the table to ensure that data was correctly loaded

Here you will use the CassandraSQLContext (csc) to use SparkSQL against the table you have created and populated.

  * Start by verifying the number of rows that were loaded into the table. Notice that we are chaining the methods together to make this a single command. The result of the query should be 3606, consistent with the earlier results.

        csc.sql(s"SELECT COUNT(*) FROM spark_cass.station_tmax").first.foreach(println)
  * Next validate that the data was loaded as expected. Notice here the use of the take method. This limits the amount of data returned.

        csc.sql(s"SELECT * FROM spark_cass.station_tmax").take(10).foreach(println)

        [USC00047767,20030101,1,TMAX,1,144,2003]
        [USC00047767,20030102,2,TMAX,1,144,2003]
        [USC00047767,20030103,3,TMAX,1,167,2003]
        [USC00047767,20030104,4,TMAX,1,167,2003]
        [USC00047767,20030105,5,TMAX,1,178,2003]
        [USC00047767,20030106,6,TMAX,1,194,2003]
        [USC00047767,20030107,7,TMAX,1,178,2003]
        [USC00047767,20030108,8,TMAX,1,172,2003]
        [USC00047767,20030110,10,TMAX,1,156,2003]
        [USC00047767,20030111,11,TMAX,1,150,2003]
  * Finally take advantage of the SparkSQL and issue a query that uses filters on fields that are not Cassandra primary key, cluster key or indexed fields.

        csc.sql(s"SELECT * FROM spark_cass.station_tmax WHERE year=2003 AND month=12").collect.foreach(println)

