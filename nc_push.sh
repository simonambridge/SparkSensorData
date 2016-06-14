for i in `cat SensorData2.csv` 
do
echo $i | netcat -lk 8887
# # # 
sleep 1
echo $i
done
