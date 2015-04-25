name := "SparkPortStream"

version := "1.0"

scalaVersion := "2.10.4"

organization := "datastax.com"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-twitter" % "1.1.0"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.1.0"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "3.0.3"

libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

libraryDependencies += "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.1.1"

libraryDependencies += "com.xeiam.xchart" % "xchart" % "2.4.3"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

resolvers += "erichseifert.de" at "http://mvn.erichseifert.de/maven2"
    
