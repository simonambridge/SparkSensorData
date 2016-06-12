echo "Parameters are: <cassandra host> <spark master host> <data port>"
dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar $1 $2 $3
