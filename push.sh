for i in `cat SensorData2.csv` 
do
echo $i | nc -l 9999
# # # 
sleep 1
echo $i
done
