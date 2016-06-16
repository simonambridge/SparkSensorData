# Simple example using netCat - 
echo "Parameters are: <linear or non-linear (random) l|n > <rate in ms> <number of samples> <port to stream to>"
if [[ $# -eq 0 ]] ; then
	 echo "No parameters supplied"
	   echo "For a set of 100 random data points every 500ms on port 9999 try:"
	   echo "./netCat n 500 100 9999"
	     exit 1
     fi


java -cp  /u02/dev/dse_dev/SparkSensorData/target/scala-2.10/classes netCat $1 $2 $3 $4
