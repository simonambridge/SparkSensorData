#Overview

In this step we will perform the following steps:

1. Start DSE/cassandra with Spark enabled and connect to the Spark command line REPL
2. Prepare a Cassandra keyspace and table for new data
3. Create a Spark RDD with data and validate that information
4. Insert the contents of the RDD into the Cassandra table

#Requirements

Local copy of DSE 4.6 installed (This example is based on a tarball install on Mac OS X)

##1. Start DSE/cassandra with Spark enabled and connect to the Spark command line REPL

  1. From the command line, navigate to the directory where DSE is install
  2. Issue the command "bin/dse cassandra -k" This will start cassandra with spark enabled
  3. Issue the command "bin/dse spark" This will bring you to the DSE/Spark REPL. Your prompt should look like: "scala>"

###Observations
At this point you will have the following screen:

    Welcome to
          ____              __
         / __/__  ___ _____/ /__
        _\ \/ _ \/ _ `/ __/  '_/
       /___/ .__/\_,_/_/ /_/\_\   version 1.1.0
          /_/

    Using Scala version 2.10.4 (Java HotSpot(TM) 64-Bit Server VM, Java 1.7.0_71)
    Type in expressions to have them evaluated.
    Type :help for more information.
    Creating SparkContext...
    Initializing SparkContext with MASTER: spark://127.0.0.1:7077
    Created spark context..
    Spark context available as sc.
    HiveSQLContext available as hc.
    CassandraSQLContext available as csc.
    Type in expressions to have them evaluated.
    Type :help for more information.

    scala>

Notice the three different contexts. We will be focused on using the Spark context (sc) and the CassandraSQLContext (csc) in our work.

##2. Prepare a Cassandra keyspace and table for new data

To insert data into a cassandra table from Spark a table must already exist or be created as part of the work flow. You can accomplish this through the CQL shell or via issuing command from the Spark REPL/job. This example will focus on using the Spark REPL. We will issue the following CQL commands

    "CREATE KEYSPACE IF NOT EXISTS spark_cass WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}"
    "DROP TABLE IF EXISTS spark_cass.simple"
    "CREATE TABLE IF NOT EXISTS spark_cass.simple (pk_id integer, value text, PRIMARY KEY(pk_id))"

The first thing we will need to do to make this work is import the library that allows us to establish a connection and session to Cassandra


  * At the Spark REPL command line type this command

        import com.datastax.spark.connector.cql.CassandraConnector

  * Now we need to use this library to create a connection to Cassandra

        val connector = CassandraConnector(csc.conf)

  * Next we need to create a session with Cassandra

        val session = connector.openSession()

  * With the session created we can now execute the CQL statement that will prepare our keyspace and table. Execute each of the below statements.

        session.execute(s"CREATE KEYSPACE IF NOT EXISTS spark_cass WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}")
        session.execute(s"DROP TABLE IF EXISTS spark_cass.simple")
        session.execute(s"CREATE TABLE IF NOT EXISTS spark_cass.simple (pk_id int, value text, PRIMARY KEY(pk_id))")

  * We are now finished with the Cassandra session/connector so we can free those resources

        session.close()

At this point you should have created a keyspace and a table that we can now use.

IMPORTANT NOTE: In most of your use case you will not need to go through step 2 as you keyspaces and tables will already exist.

##3. Create a Spark RDD with data and validate that information

First you need to create some data to insert into out simple table. Recall the table is an integer primary key and a text value.

  * From the Spark REPL, issue the below command to create and RDD named data. Note that this command uses the Spark Context (sc) not the Cassandra Spark Context (csc).

        val data = sc.parallelize(List((1,"abc"),(2,"def"),(3,"ghi")))

  * You can verify that you have created this data with the following commands

    * Count the number of rows you created (result should be 3)

        data.count()

    * Display the contents of all the rows in the RDD

        data.collect.foreach(println)

      * OR BETTER (collect can be very expensive with large partitioned data sets)

        data.take(3).foreach(println)


##4. Insert the contents of the RDD into the Cassandra table

Here you will take the RDD that you created (data) and save it to the table we prepared (simple) in the spark_cass keyspace.

  * This is a single step process where we call the "saveToCassandra" method on the RDD object.

        data.saveToCassandra("spark_cass", "simple", SomeColumns("pk_id", "value"))

  * Now query the contents of the table. Here we will use the CassandraSQLContext (csc) and it SQL method. We will save the results ot a new RDD.

        val cassData = csc.sql(s"SELECT * FROM spark_cass.simple")

  * Validate the contents of the new RDD

        cassData.count()

  * Print the contents of the new RDD

        cassData.collect.foreach(println)

You can also test that this data has been inserted into the table via the CQL shell.

