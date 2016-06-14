echo "Parameters are: <cassandra host> <spark master host> <data port>"
if [[ $# -eq 0 ]] ; then
 echo "Mo parameters supplied"
  echo "Try: inget_run.sh 127.0.0.1 127.0.0.1 9999"
  exit 1
fi

dse spark-submit --class SparkIngest ./target/scala-2.10/sparkportstream_2.10-1.0.jar $1 $2 $3
