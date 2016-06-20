name := "SparkSensorData"

version := "1.0"

scalaVersion := "2.10.4"

organization := "datastax.com"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.6" % "provided"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.4.0" % "provided"

libraryDependencies += "org.apache.spark" % "spark-sql_2.10" % "1.4.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.4.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming-twitter" % "1.4.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.4.0" % "provided"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3" % "provided"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "3.0.3" % "provided"

libraryDependencies += "commons-cli" % "commons-cli" % "1.2" % "provided"

libraryDependencies += "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.4.1" % "provided" 

libraryDependencies += "org.knowm.xchart" % "xchart" % "3.0.4" exclude("de.erichseifert.vectorgraphics2d", "VectorGraphics2D") withSources()

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

resolvers += "erichseifert.de" at "http://mvn.erichseifert.de/maven2"
    
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
