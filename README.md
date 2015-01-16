#Introduction

In this set of exercises we will walk through the process of loading data into Cassandra using Spark. The process will be broken down into several parts that build upon one another. Please review these in order. If something does not work in one section it may be due to piecies that were built in a previous session.

#Prerequisites
These exercises were all built using DSE 4.6. At a minimum you should be using that version of DSE. For this series of exercises we will be running all of the examples against a single node cluster.

##1. First Steps: Basic interaction with Cassandra using the Spark Command Line (REPL)

In this exercise you will use the Spark Command Line REPL that is part of DSE to interact with Cassandra using Spark/Scala. Specifically you will perform the following activities:

  * Start DSE/cassandra with Spark enabled and connect to the Spark command line REPL
  * Prepare a Cassandra keyspace and table for new data
  * Create a Spark RDD with data and validate that information
  * Insert the contents of the RDD into the Cassandra table

Please proceed to the file [FirstSteps.md](./FirstSteps.md)

##2. Use Spark Command Line (REPL) to load and manipulate local file data using Spark and SparkSQL

In this exercise you will perform the following steps:

  * Locate and review the source data for the new tables
  * Prepare a Cassandra table in the spark_cass keyspace for the new data
  * Create a spark RDD from the data in the file and load it into the Cassandra table
  * Query the table to ensure that data was correctly loaded

Please proceed to the file [LoadFromLocalFileREPL.md](./LoadFromLocalFileREPL.md)

