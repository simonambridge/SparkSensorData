#Overview

The goal of this exercise is to build and run a Spark program using Scala that will read several local files and load them into native cassandra tables on a Spark enabled Cassandra cluster.

In this exercise you will perform the following steps:

1. Clone a GitHub repository to your local machine
2. Ensure that you have sbt installed and accessible on your machine
3. Find and edit the Scala code example to ensure it is configured for your environment
4. Use sbt to build and run the example on your Cassandra/Spark cluster
5. Use SparkSQL from the DSE Spark REPL to validate the data loaded into your cluster

#Requirements

Local copy of DSE 4.6 installed (This example is based on a tarball install on Mac OS X). Make sure you can get to and interact with the Spark REPL included with DSE.

You need to have a GitHub id and git installed on your local machine. Further, you must be able to clone GitHub repositories to your local machine.

The ability to install sbt on your local machine is also required.

##1. Clone a GitHub repository to your local machine

Navigate to a directory that you would like to use for this project. From the command line in that directory issue the following command

                git https://github.com/CaryBourgeois/DSE-Spark-HandsOn.git

Review the directory to ensure that you have downloaded all of the files from the repository.

##2. Ensure that you have sbt installed and accessible on your machine

This program uses the Scala Build Tool (sbt) to build and run the code in this exercise. For this to work sbt must be installed and on the executable path of your system.

To validate sbt is installed on your system you should be able to go to the command line and execute the following commands and get these or similar results.

        $>sbt sbt-version
        [info] Set current project to bin (in build file:/Users/carybourgeois/bin/)
        [info] 0.13.5
If this is not the case then please visit the [sbt site](http://www.scala-sbt.org/) for instructions on how to download and install sbt.

##3. Find and edit the Scala code example to ensure that it is configured for your environment

This project was created with the Community Edition of IntelliJ IDEA. The simplest way to get review and modify the scala code is to open the project with this IDE. Alternatively, you can use any text editor to view/edit the file as the build and execute process will work fomr the command line via sbt.

From the directory where you cloned the github project, navigate to the `/src/main/scala` directory. Locate and open the file `LoadFromLocalFile.scala` file.

This is a very simple Scala/Spark example. It contain one object and `main` method in that object. Within that method there are five import segments of the code:

  * Setup the connection to Spark
    * Specify the Spark configuration parameters (These will have to be modified to fit your environment)
    * Create the Spark Context based on the Spark Configuration
  * Prepare the Cassandra keyspace and tables for the new data
    * Obtain a native connection to Cassandra
    * Verify/create the keyspace on the cluster
    * Drop tables if they already exist and create new tables to recieve the data
    * Close the session variable as it will not be needed again.
  * Read local files and put the contents into the tables
    * Read the local files into a Spark RDD
    * Parse the lines based on the delimiter into another RDD
    * Save the RDD containing the parsed lines into Cassandra using the `saveToCassandra` method
  * Use SparkSQL to validate the load inserted the correct number of records
  * Clean up and exit making sure to release the Spark Context

Once you have reviewed the code you will need to make changes to reflect your specific system.

  * Locate the SparkConf settings and modify the ip to reflect your system. If you are running a local copy of DSE no changes will be required. If not, you will need to substitute your server's ip address in place of `127.0.0.1`.

        val sparkConf = new SparkConf().set("spark.cassandra.connection.host", "127.0.0.1")
              .setJars(Array("target/scala-2.10/DSE-Spark-HandsOn-assembly-1.0.jar"))
              .setMaster("spark://127.0.0.1:7077")
              .setAppName("DSE Spark HandsOn")
              .set("spark.eventLog.enabled", "false")
  * Locate the section of the program where the local files are read and substitute the path of your local files for the one that is there.

        var lines = sc.textFile(s"file:///Users/carybourgeois/Documents/Training/DSE-Spark-HandsOn/data/sftmax_2003_2013-load.csv")
  * Save the changes to the file.

##4. Use sbt to build and run the example on your Cassandra/Spark cluster

You will now use sbt to build and run the file you have modified.

  * Run sbt to build the project using the command below form the command line. This command will compile for file we created and build the "fat" jar that will be copied to the Spark master for execution. This could be a lengthy process as sbt probably have to download a number of files. At the end of the process you should have a response of `[success]`

        sbt assembley
  * Run the project using the sbt run command. This will copy the "fat" jar to the Spark system and execute the program. The output will contain a bunch of [INFO] entries and conclude with [success] is all has gone well.

        sbt run
  * You can look at the status of your job using Spark Web UI. You get to this usinf the URL http://\<your ip address\>:7080. If you are using a local DSE/Spark environment the link would be [http://127.0.0.1:7080](http://127.0.0.1:7080)

NOTE: One of the more challenging part of running a program on a spark cluster is building the "fat" jar that contains the libraries you need while not duplicating those on the Spark system and causing conflicts. This is managed by the `build.sbt` file. If you plan on doing much of this work you should read and understand how this porcess works.

##5. Use SparkSQL from the DSE Spark REPL to validate the data loaded into your cluster

The next step is to go back to the DSE Spark REPL and validate that the data was loaded correctly.

  * For the Maximum Temperature table run the following command. The result should be 3606.

        csc.sql(s"SELECT COUNT(*) FROM spark_cass.station_tmax").first.foreach(println)

  * For the Minimum Temperature table run the following command. The result should be 3583.

        csc.sql(s"SELECT COUNT(*) FROM spark_cass.station_tmin").first.foreach(println)

  * For the Precipitation table run the following command. The result should be 3350.

            csc.sql(s"SELECT COUNT(*) FROM spark_cass.station_prcp").first.foreach(println)

EXTRA CREDIT: Try to write a SparkSQL command that Joins two of the three table to yield a result set with the st_id, yyyymmdd, tmax, tmin.

This is one way to do it.

        csc.sql(s"SELECT tmax.st_id, tmax.yyyymmdd, tmax.value AS tmax, tmin.value AS tmin FROM spark_cass.station_tmax tmax INNER JOIN spark_cass.station_tmin tmin ON tmax.st_id = tmin.st_id AND tmax.yyyymmdd = tmin.yyyymmdd").take(10).foreach(println)

